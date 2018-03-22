% test_dqpsk2.m
% David Rowe April 2014
%
% DQPSK modem simulation inclduing filtering to test modulating modem
% tx power based on speech energy.  Unlike test_dpsk runs at sample
% rate Fs.

1;

% main test function 

function sim_out = ber_test(sim_in)
    Fs = 8000;

    verbose          = sim_in.verbose;
    framesize        = sim_in.framesize;
    Ntrials          = sim_in.Ntrials;
    Esvec            = sim_in.Esvec;
    phase_offset     = sim_in.phase_offset;
    w_offset         = sim_in.w_offset;
    plot_scatter     = sim_in.plot_scatter;
    Rs               = sim_in.Rs;
    hf_sim           = sim_in.hf_sim;
    Nhfdelay         = floor(sim_in.hf_delay_ms*Fs/1000);
    Nc               = sim_in.Nc;
    symbol_amp       = sim_in.symbol_amp;

    bps              = 2;
    Nsymb            = framesize/bps;
    for k=1:Nc
      prev_sym_tx(k) = qpsk_mod([0 0]);
      prev_sym_rx(k) = qpsk_mod([0 0]);
    end

    % design root nyquist (root raised cosine) filter and init tx and rx filter states

    alpha = 0.5; T=1/Fs; Nfiltsym=7; M=Fs/Rs;
    if floor(Fs/Rs) != Fs/Rs
        printf("oversampling ratio must be an integer\n");
        return;
    end
    hrn = gen_rn_coeffs(alpha, T, Rs, Nfiltsym, M);
    Nfilter = length(hrn);

    % convert "spreading" samples from 1kHz carrier at Fs to complex
    % baseband, generated by passing a 1kHz sine wave through PathSim
    % with the ccir-poor model, enabling one path at a time.
    
    Fc = 1000;
    fspread = fopen("../raw/sine1k_2Hz_spread.raw","rb");
    spread1k = fread(fspread, "int16")/10000;
    fclose(fspread);
    fspread = fopen("../raw/sine1k_2ms_delay_2Hz_spread.raw","rb");
    spread1k_2ms = fread(fspread, "int16")/10000;
    fclose(fspread);

    % down convert to complex baseband
    spreadbb = spread1k.*exp(-j*(2*pi*Fc/Fs)*(1:length(spread1k))');
    spreadbb_2ms = spread1k_2ms.*exp(-j*(2*pi*Fc/Fs)*(1:length(spread1k_2ms))');

    % remove -2000 Hz image
    b = fir1(50, 5/Fs);
    spread = filter(b,1,spreadbb);
    spread_2ms = filter(b,1,spreadbb_2ms);

    % discard first 1000 samples as these were near 0, probably as
    % PathSim states were ramping up.  Transpose for convenience

    spread     = transpose(spread(1000:length(spread)));
    spread_2ms = transpose(spread_2ms(1000:length(spread_2ms)));

    % Determine "gain" of HF channel model, so we can normalise
    % carrier power during HF channel sim to calibrate SNR.  I imagine
    % different implementations of ccir-poor would do this in
    % different ways, leading to different BER results.  Oh Well!

    hf_gain = 1.0/sqrt(var(spread)+var(spread_2ms));
        
    % Start Simulation ----------------------------------------------------------------

    for ne = 1:length(Esvec)
        EsNodB = Esvec(ne);
        EsNo = 10^(EsNodB/10);
    
        variance = Fs/(Rs*EsNo);
         if verbose > 1
            printf("EsNo (dB): %f EsNo: %f variance: %f\n", EsNodB, EsNo, variance);
        end
        
        Terrs = 0;  Tbits = 0;

        tx_symb_log             = [];
        rx_symb_log             = [];
        noise_log               = [];
        sim_out.errors_log      = [];
        sim_out.tx_baseband_log = [];
        sim_out.rx_filt_log     = [];
        symbol_amp_index        = 1;
       
        % init filter memories and LOs

        tx_filter_memory  = zeros(Nc, Nfilter);
        rx_filter_memory  = zeros(Nc, Nfilter);
        s_delay_line_filt = zeros(Nc, Nfiltsym);
        phase_tx = ones(1,Nc);
        phase_rx = ones(1,Nc);
        Fcentre = 1500; Fsep = (1+alpha)*Rs;
        freq = Fcentre + Fsep*((-Nc/2+0.5):(Nc/2-0.5));
        freq = exp(j*freq*2*pi/Fs);

        % init HF channel

        sc = 1; hf_n = 1;
        hf_sim_delay_line = zeros(1,M+Nhfdelay);
        freq_sample_hz = Fcentre + ((Fsep*(-Nc/2)):50:(Fsep*(Nc/2)));
        freq_sample_rads = (2*pi/Fs)*freq_sample_hz;
        hf_model = ones(Ntrials*Nsymb/Nc, length(freq_sample_rads));   % defaults for plotting surface

        % bunch of outputs we log for graphing

        sim_out.errors_log = [];
        sim_out.Nerrs = [];
        sim_out.snr_log = [];
        sim_out.hf_model_pwr = [];
        sim_out.tx_fdm_log = [];
        C_log = [];

        for nn = 1: Ntrials
                  
            tx_bits = round( rand( 1, framesize ) );
 
            % modulate --------------------------------------------

            s = zeros(1, Nsymb);
            for i=1:Nc:Nsymb
              for k=1:Nc
                tx_symb = qpsk_mod(tx_bits(2*(i-1+k-1)+1:2*(i+k-1)));
                s_qpsk(i+k-1) = tx_symb;
                tx_symb *= prev_sym_tx(k);
                prev_sym_tx(k) = tx_symb;
                s(i+k-1) = symbol_amp(symbol_amp_index)*tx_symb;
              end
            end
            symbol_amp_index++;
            s_ch = s;

            % Now we start processing frame Nc symbols at a time to model parallel carriers

            tx_fdm_sym_log = [];
            for i=1:Nc:Nsymb

                % Delay tx symbols to match delay due to filters. qpsk
                % (rather than dqpsk) symbols used for convenience as
                % it's easy to shift symbols than pairs of bits

                s_delay_line_filt(:,1:Nfiltsym-1) = s_delay_line_filt(:,2:Nfiltsym);
                s_delay_line_filt(:,Nfiltsym) = s_qpsk(i:i+Nc-1);
                s_qpsk(i:i+Nc-1) = s_delay_line_filt(:,1);  
                for k=1:Nc
                    tx_bits(2*(i-1+k-1)+1:2*(i+k-1)) = qpsk_demod(s_qpsk(i+k-1));
                end

                % tx filter

                tx_baseband = zeros(Nc,M);

                % tx filter each symbol, generate M filtered output samples for each symbol.
                % Efficient polyphase filter techniques used as tx_filter_memory is sparse

                tx_filter_memory(:,Nfilter) = s(i:i+Nc-1);

                for k=1:M
                    tx_baseband(:,k) = M*tx_filter_memory(:,M:M:Nfilter) * hrn(M-k+1:M:Nfilter)';
                end
                tx_filter_memory(:,1:Nfilter-M) = tx_filter_memory(:,M+1:Nfilter);
                tx_filter_memory(:,Nfilter-M+1:Nfilter) = zeros(Nc,M);

                sim_out.tx_baseband_log = [sim_out.tx_baseband_log  tx_baseband];

                % upconvert
 
                tx_fdm = zeros(1,M);

                for c=1:Nc
                    for k=1:M
                        phase_tx(c) = phase_tx(c) * freq(c);
	                tx_fdm(k) = tx_fdm(k) + tx_baseband(c,k)*phase_tx(c);
                    end
                end
 
                sim_out.tx_fdm_log = [sim_out.tx_fdm_log tx_fdm];
 
                % HF channel
                
                if hf_sim
                   hf_sim_delay_line(1:Nhfdelay) = hf_sim_delay_line(M+1:M+Nhfdelay);
                   hf_sim_delay_line(Nhfdelay+1:M+Nhfdelay) = tx_fdm;

                   tx_fdm = tx_fdm.*spread(sc:sc+M-1) + hf_sim_delay_line(1:M).*spread_2ms(sc:sc+M-1);
                   tx_fdm *= hf_gain;

                   % sample HF channel spectrum in middle of this symbol for plotting

                   hf_model(hf_n,:) = hf_gain*(spread(sc+M/2) + exp(-j*freq_sample_rads*Nhfdelay)*spread_2ms(sc+M/2));

                   sc += M;
                   hf_n++;
                end

                tx_fdm_sym_log = [tx_fdm_sym_log tx_fdm ];

                % AWGN noise and phase/freq offset channel simulation
                % 0.5 factor ensures var(noise) == variance , i.e. splits power between Re & Im

                noise = sqrt(variance*0.5)*(randn(1,M) + j*randn(1,M));
                noise_log = [noise_log noise];

                % apply frequency and phase offset and noise

                for k=1:M
                    rx_fdm(k) = tx_fdm(k)*exp(j*phase_offset) + noise(k);
                    phase_offset += w_offset;
                end

                % downconvert

                rx_baseband = zeros(Nc,M);
                for c=1:Nc
                    for k=1:M
                        phase_rx(c) = phase_rx(c) * freq(c);
	                rx_baseband(c,k) = rx_fdm(k)*phase_rx(c)';
                    end
                end

                % rx filter

                rx_filter_memory(:,Nfilter-M+1:Nfilter) = rx_baseband;
                rx_filt = rx_filter_memory * hrn';
                rx_filter_memory(:,1:Nfilter-M) = rx_filter_memory(:,1+M:Nfilter);
                sim_out.rx_filt_log = [sim_out.rx_filt_log rx_filt];

                s_ch(i:i+Nc-1) = rx_filt;
            end

            % est HF model power for entire code frame (which could be several symbols)

            if hf_sim
              frame_hf_model = reshape(hf_model(hf_n-Nsymb/Nc:hf_n-1,:),1,(Nsymb/Nc)*length(freq_sample_hz));                       
              sim_out.hf_model_pwr = [sim_out.hf_model_pwr mean(abs(frame_hf_model).^2)];
            else 
              sim_out.hf_model_pwr = [sim_out.hf_model_pwr 1];
            end

            % "genie" SNR estimate 
            
            snr = (tx_fdm_sym_log*tx_fdm_sym_log')/(M*variance);
            sim_out.snr_log = [sim_out.snr_log snr];
  
            % de-modulate

            rx_bits = zeros(1, framesize);
            for i=1:Nc:Nsymb
              for k=1:Nc
                rx_symb = s_ch(i+k-1);
                tmp = rx_symb;
                rx_symb *= conj(prev_sym_rx(k)/abs(prev_sym_rx(k)));
                prev_sym_rx(k) = tmp;
                rx_bits((2*(i-1+k-1)+1):(2*(i+k-1))) = qpsk_demod(rx_symb);
                rx_symb_log = [rx_symb_log rx_symb];
              end
            end

            % ignore data until we have enough frames to fill filter memory
            % then count errors

            if nn > ceil(Nfiltsym/(Nsymb/Nc))
                error_positions = xor(rx_bits, tx_bits);
                sim_out.errors_log = [sim_out.errors_log error_positions];
                Nerrs = sum(error_positions);
                sim_out.Nerrs = [sim_out.Nerrs Nerrs];
                Terrs += Nerrs;
                Tbits += length(tx_bits);
            end

        end

        TERvec(ne) = Terrs;
        BERvec(ne) = Terrs/Tbits;

        if verbose 
            printf("EsNo (dB): %f  Terrs: %d BER %f ", EsNodB, Terrs, Terrs/Tbits);
            printf("\n");
        end
        if verbose > 1
            printf("Terrs: %d BER %f C %f N %f Es %f No %f Es/No %f\n\n", Terrs,
                   Terrs/Tbits, var(sim_out.tx_fdm_log), var(noise_log),
                   var(sim_out.tx_fdm_log)/(Nc*Rs), var(noise_log)/Fs, (var(sim_out.tx_fdm_log)/(Nc*Rs))/(var(noise_log)/Fs));
        end
    end
    
    Ebvec = Esvec - 10*log10(bps);

    sim_out.BERvec = BERvec;
    sim_out.Ebvec  = Ebvec;
    sim_out.TERvec = TERvec;

    if plot_scatter
        figure(2);
        clf;
        scat = rx_symb_log(Nfiltsym*Nc:length(rx_symb_log)) .* exp(j*pi/4);
        plot(real(scat), imag(scat),'+');
        title('Scatter plot');

        figure(3);
        clf;        
        y = 1:Rs*2;
        EsNodBSurface = 20*log10(abs(hf_model(y,:))) + EsNodB;
        mesh(1:length(freq_sample_hz),y,EsNodBSurface);
        grid
        title('HF Channel Es/No');
    end

endfunction

% Gray coded QPSK modulation function

function symbol = qpsk_mod(two_bits)
    two_bits_decimal = sum(two_bits .* [2 1]); 
    switch(two_bits_decimal)
        case (0) symbol =  1;
        case (1) symbol =  j;
        case (2) symbol = -j;
        case (3) symbol = -1;
    endswitch
endfunction

% Gray coded QPSK demodulation function

function two_bits = qpsk_demod(symbol)
    if isscalar(symbol) == 0
        printf("only works with scalars\n");
        return;
    end
    bit0 = real(symbol*exp(j*pi/4)) < 0;
    bit1 = imag(symbol*exp(j*pi/4)) < 0;
    two_bits = [bit1 bit0];
endfunction

function sim_in = standard_init
  sim_in.verbose          = 1;
  sim_in.plot_scatter     = 0;

  sim_in.Esvec            = 5:15; 
  sim_in.Ntrials          = 100;
  sim_in.framesize        = 64;
  sim_in.Rs               = 100;
  sim_in.Nc               = 8;

  sim_in.phase_offset     = 0;
  sim_in.w_offset         = 0;
  sim_in.phase_noise_amp  = 0;

  sim_in.hf_delay_ms      = 2;
  sim_in.hf_sim           = 0;
  sim_in.hf_phase_only    = 0;
  sim_in.hf_mag_only      = 0;
endfunction

function awgn_hf_ber_curves()
  sim_in = standard_init();

  Ebvec = sim_in.Esvec - 10*log10(2);
  BER_theory = 0.5*erfc(sqrt(10.^(Ebvec/10)));

  dpsk_awgn = ber_test(sim_in);
  sim_in.hf_sim           = 1;
  dpsk_hf   = ber_test(sim_in);

  figure(1); 
  clf;
  semilogy(Ebvec, BER_theory,'r;QPSK theory;')
  hold on;
  semilogy(dpsk_awgn.Ebvec, dpsk_awgn.BERvec,'g;DQPSK;')
  semilogy(dpsk_hf.Ebvec, dpsk_hf.BERvec,'g;DQPSK HF;')
  hold off;
  xlabel('Eb/N0')
  ylabel('BER')
  grid("minor")
  axis([min(Ebvec) max(Ebvec) 1E-3 1])
end

sim_in = standard_init();

% energy file sampled every 10ms

load ../src/ve9qrp.txt
pdB=10*log10(ve9qrp);
for i=1:length(pdB)
  if pdB(i) < 0
    pdB(i) = 0;
  end
end

% Down sample to 40ms rate used for 1300 bit/s codec, every 4th sample is transmitted

pdB = pdB(4:4:length(pdB));

% Use linear mapping function in dB domain to map to symbol power

power_map_x  = [ 0 20 24 40 50 ];
power_map_y  = [-6 -6  0 6  6];
mapped_pdB = interp1(power_map_x, power_map_y, pdB);

sim_in.symbol_amp = 10 .^ (mapped_pdB/20);
%sim_in.symbol_amp = ones(1,length(pdB));
sim_in.plot_scatter = 1;
sim_in.verbose      = 2;
sim_in.hf_delay_ms  = 2;
sim_in.hf_sim       = 1;
sim_in.Esvec        = 10;
sim_in.Ntrials      = 400;

dqpsk_pwr_hf = ber_test(sim_in);

% note: need way to test that power is aligned with speech

figure(4)
clf;
plot((1:sim_in.Ntrials)*80*4, pdB(1:sim_in.Ntrials));
hold on;
plot((1:sim_in.Ntrials)*80*4, mapped_pdB(1:sim_in.Ntrials),'r');
hold off;

figure(5)
clf;
s = load_raw("../raw/ve9qrp.raw");
M=320; M_on_2 = M/2; % processing delay between input speech and centre of analysis window
subplot(211)
plot(M_on_2:(M_on_2-1+sim_in.Ntrials*M),s(1:sim_in.Ntrials*M))
hold on;
plot((1:sim_in.Ntrials)*M, 5000*sim_in.symbol_amp(1:sim_in.Ntrials),'r');
hold off;
axis([1 sim_in.Ntrials*M -3E4 3E4]);
subplot(212)
plot(real(dqpsk_pwr_hf.tx_fdm_log));


figure(6)
clf;
plot((1:sim_in.Ntrials)*M, 20*log10(sim_in.symbol_amp(1:sim_in.Ntrials)),'b;Es (dB);');
hold on;
plot((1:sim_in.Ntrials)*M, 10*log10(dqpsk_pwr_hf.hf_model_pwr),'g;Fading (dB);');
plot((1:sim_in.Ntrials)*M, 10*log10(dqpsk_pwr_hf.snr_log),'r;Es/No (dB);');

ber = dqpsk_pwr_hf.Nerrs/sim_in.framesize;
ber_clip = ber;
ber_clip(find(ber > 0.2)) = 0.2;
plot((1:length(ber_clip))*M, -20+100*ber_clip,'k;BER (0-20%);');
hold off;
axis([1 sim_in.Ntrials*M -20 20])

fep=fopen("dqpsk_errors_pwr.bin","wb"); fwrite(fep, dqpsk_pwr_hf.errors_log, "short"); fclose(fep);
fber=fopen("ber.bin","wb"); fwrite(fber, ber, "float"); fclose(fber);
