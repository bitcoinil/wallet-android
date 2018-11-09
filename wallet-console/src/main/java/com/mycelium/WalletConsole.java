package com.mycelium;

import com.mrd.bitlib.StandardTransactionBuilder;
import com.mrd.bitlib.UnsignedTransaction;
import com.mrd.bitlib.crypto.Bip39;
import com.mrd.bitlib.crypto.BitcoinSigner;
import com.mrd.bitlib.crypto.IPrivateKeyRing;
import com.mrd.bitlib.crypto.IPublicKeyRing;
import com.mrd.bitlib.crypto.InMemoryPrivateKey;
import com.mrd.bitlib.crypto.PublicKey;
import com.mrd.bitlib.crypto.RandomSource;
import com.mrd.bitlib.model.Address;
import com.mrd.bitlib.model.AddressType;
import com.mrd.bitlib.model.NetworkParameters;
import com.mrd.bitlib.model.ScriptOutput;
import com.mrd.bitlib.model.Transaction;
import com.mrd.bitlib.model.UnspentTransactionOutput;
import com.mycelium.net.HttpEndpoint;
import com.mycelium.net.HttpsEndpoint;
import com.mycelium.net.ServerEndpoints;
import com.mycelium.wapi.api.Wapi;
import com.mycelium.wapi.api.WapiClientElectrumX;
import com.mycelium.wapi.api.jsonrpc.TcpEndpoint;
import com.mycelium.wapi.model.TransactionOutputEx;
import com.mycelium.wapi.model.TransactionOutputSummary;
import com.mycelium.wapi.wallet.AccountListener;
import com.mycelium.wapi.wallet.AesKeyCipher;
import com.mycelium.wapi.wallet.BTCSettings;
import com.mycelium.wapi.wallet.Currency;
import com.mycelium.wapi.wallet.CurrencySettings;
import com.mycelium.wapi.wallet.KeyCipher;
import com.mycelium.wapi.wallet.Reference;
import com.mycelium.wapi.wallet.SecureKeyValueStore;
import com.mycelium.wapi.wallet.SendRequest;
import com.mycelium.wapi.wallet.SyncMode;
import com.mycelium.wapi.wallet.WalletAccount;
import com.mycelium.wapi.wallet.WalletManager;
import com.mycelium.wapi.wallet.bip44.ChangeAddressMode;
import com.mycelium.wapi.wallet.btc.BtcAddress;
import com.mycelium.wapi.wallet.btc.BtcLegacyAddress;
import com.mycelium.wapi.wallet.btc.InMemoryWalletManagerBacking;
import com.mycelium.wapi.wallet.btc.WalletManagerBacking;
import com.mycelium.wapi.wallet.btc.bip44.AdditionalHDAccountConfig;
import com.mycelium.wapi.wallet.btc.bip44.BitcoinHDModule;
import com.mycelium.wapi.wallet.btc.bip44.ExternalSignatureProviderProxy;
import com.mycelium.wapi.wallet.btc.bip44.HDAccount;
import com.mycelium.wapi.wallet.btc.single.BitcoinSingleAddressModule;
import com.mycelium.wapi.wallet.btc.single.PublicPrivateKeyStore;
import com.mycelium.wapi.wallet.btc.single.SingleAddressAccount;
import com.mycelium.wapi.wallet.masterseed.MasterSeedManager;
import com.mycelium.wapi.wallet.coins.Value;
import com.mycelium.wapi.wallet.colu.ColuAccount;
import com.mycelium.wapi.wallet.colu.ColuApiImpl;
import com.mycelium.wapi.wallet.colu.ColuClient;
import com.mycelium.wapi.wallet.colu.ColuModule;
import com.mycelium.wapi.wallet.colu.ColuSendRequest;
import com.mycelium.wapi.wallet.colu.ColuTransaction;
import com.mycelium.wapi.wallet.colu.PrivateColuConfig;
import com.mycelium.wapi.wallet.colu.coins.RMCCoin;
import com.mycelium.wapi.wallet.eth.EthAccount;
import com.mycelium.wapi.wallet.eth.EthAddress;
import com.mycelium.wapi.wallet.eth.EthTransaction;
import com.mycelium.wapi.wallet.eth.coins.EthMain;
import com.mycelium.wapi.wallet.exceptions.TransactionBroadcastException;
import com.mycelium.wapi.wallet.manager.Synchronizer;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class WalletConsole {

    public static final String[] ColoredCoinsApiURLs = {"http://testnet.api.coloredcoins.org/v3/"};
    public static final String[] ColuBlockExplorerApiURLs = {"http://testnet.explorer.coloredcoins.org/api/"};
//    public static final String[] ColoredCoinsApiURLs = {"https://coloredcoinsd.gear.mycelium.com/v3/", "https://api.coloredcoins.org/v3/"};
//    public static final String[] ColuBlockExplorerApiURLs = {"https://coloredcoins.gear.mycelium.com/api/", "https://explorer.coloredcoins.org/api/"};


    private static class MyRandomSource implements RandomSource {
        SecureRandom _rnd;

        MyRandomSource() {
            _rnd = new SecureRandom(new byte[]{42});
        }

        @Override
        public void nextBytes(byte[] bytes) {
            _rnd.nextBytes(bytes);
        }
    }

    public static void main(String[] args) {
        WalletManagerBacking backing = new InMemoryWalletManagerBacking();

        final ServerEndpoints testnetWapiEndpoints = new ServerEndpoints(new HttpEndpoint[]{
                new HttpsEndpoint("https://mws30.mycelium.com/wapitestnet", "ED:C2:82:16:65:8C:4E:E1:C7:F6:A2:2B:15:EC:30:F9:CD:48:F8:DB"),
        });

        final WapiLogger wapiLogger = new WapiLogger() {
            @Override
            public void logError(String message) {
                new Exception(message).printStackTrace();
            }

            @Override
            public void logError(String message, Exception e) {
                new Exception(message).printStackTrace();
            }

            @Override
            public void logInfo(String message) {
                new Exception(message).printStackTrace();
            }
        };

        final TcpEndpoint[] tcpEndpoints = new TcpEndpoint[]{new TcpEndpoint("electrumx.mycelium.com", 4432)};
        Wapi wapiClient = new WapiClientElectrumX(testnetWapiEndpoints, tcpEndpoints, wapiLogger, "0");

        ExternalSignatureProviderProxy externalSignatureProviderProxy = new ExternalSignatureProviderProxy();

        SecureKeyValueStore store = new SecureKeyValueStore(backing, new MyRandomSource());

//        Bip39.MasterSeed masterSeed =  Bip39.generateSeedFromWordList(new String[]{"cliff", "battle","noise","aisle","inspire","total","sting","vital","marble","add","daring","mouse"}, "");
        Bip39.MasterSeed masterSeed = Bip39.generateSeedFromWordList(new String[]{"oil", "oil", "oil", "oil", "oil", "oil", "oil", "oil", "oil", "oil", "oil", "oil"}, "");

        NetworkParameters network = NetworkParameters.productionNetwork;
        Map<Currency, CurrencySettings> currenciesSettingsMap = new HashMap<>();

        BTCSettings btcSettings = new BTCSettings(AddressType.P2SH_P2WPKH, new Reference<>(ChangeAddressMode.P2SH_P2WPKH));
        currenciesSettingsMap.put(Currency.BTC, btcSettings);

        WalletManager walletManager = new WalletManager(
                backing,
                network,
                wapiClient);
        walletManager.setIsNetworkConnected(true);

        MasterSeedManager masterSeedManager = new MasterSeedManager(store);
        try {

            // create and add HD Module
            masterSeedManager.configureBip32MasterSeed(masterSeed, AesKeyCipher.defaultKeyCipher());
            BitcoinHDModule bitcoinHDModule = new BitcoinHDModule(backing, store, network, wapiClient, currenciesSettingsMap);
            walletManager.add(bitcoinHDModule);

            // create account
            final HDAccount hdAccount1 = (HDAccount) walletManager.getAccount(walletManager.createAccounts(new AdditionalHDAccountConfig()).get(0));
            final HDAccount hdAccount2 = (HDAccount) walletManager.getAccount(walletManager.createAccounts(new AdditionalHDAccountConfig()).get(0));
            HDAccount hdAccount3 = (HDAccount) walletManager.getAccount(walletManager.createAccounts(new AdditionalHDAccountConfig()).get(0));


            PublicPrivateKeyStore publicPrivateKeyStore = new PublicPrivateKeyStore(store);

            BitcoinSingleAddressModule bitcoinSingleAddressModule = new BitcoinSingleAddressModule(backing, publicPrivateKeyStore, network, wapiClient);
            walletManager.add(bitcoinSingleAddressModule);


            org.bitcoinj.core.NetworkParameters netParams;
            if (network.isProdnet()) {
                netParams = MainNetParams.get();
            } else if (network.isTestnet()) {
                netParams = TestNet3Params.get();
            } else {
                netParams = RegTestParams.get();
            }

            ColuClient coluClient = new ColuClient(network, ColoredCoinsApiURLs, ColuBlockExplorerApiURLs);
            ColuModule coluModule = new ColuModule(network, netParams, publicPrivateKeyStore,
                    new ColuApiImpl(coluClient), new ColuWalletBacking(), new AccountListener() {
                @Override
                public void balanceUpdated(@NotNull WalletAccount<?, ?> walletAccount) {

                }
            });

            walletManager.add(coluModule);
            List<UUID> colu = walletManager.createAccounts(new PrivateColuConfig(
                    new InMemoryPrivateKey("", network)
                    , RMCCoin.INSTANCE, AesKeyCipher.defaultKeyCipher()));

            SingleAddressAccount coluSAAccount = null;
            ColuAccount coluAccount = null;
            if (walletManager.getAccount(colu.get(0)) instanceof ColuAccount) {
                coluAccount = (ColuAccount) walletManager.getAccount(colu.get(0));
            } else {
                coluSAAccount = (SingleAddressAccount) walletManager.getAccount(colu.get(0));
            }
            if (walletManager.getAccount(colu.get(1)) instanceof ColuAccount) {
                coluAccount = (ColuAccount) walletManager.getAccount(colu.get(1));
            } else {
                coluSAAccount = (SingleAddressAccount) walletManager.getAccount(colu.get(1));
            }


            new Synchronizer(walletManager, SyncMode.NORMAL
                    , Arrays.asList(hdAccount1, hdAccount2, hdAccount3, coluAccount, coluSAAccount)).run();

//            sendBtcFrom2Account(network, hdAccount1, hdAccount2, hdAccount3);

            sendColuWithFundingAccount(coluAccount, coluSAAccount, hdAccount2);


            EthAccount ethAccount1 = new EthAccount();
            walletManager.addAccount(ethAccount1);

            EthAccount ethAccount2 = new EthAccount();
            walletManager.addAccount(ethAccount2);

            System.out.println("ETH Account 1 balance: " + ethAccount1.getAccountBalance().getSpendable().toString());
            System.out.println("ETH Account 2 balance: " + ethAccount2.getAccountBalance().getSpendable().toString());

            SendRequest<EthTransaction> sendRequest = ethAccount1.getSendToRequest((EthAddress) ethAccount2.getReceiveAddress(), Value.valueOf(EthMain.INSTANCE, 10000));

            ethAccount1.completeAndSignTx(sendRequest);
            ethAccount1.broadcastTx(sendRequest.tx);

            System.out.println("ETH Account 1 balance: " + ethAccount1.getAccountBalance().getSpendable().toString());
            System.out.println("ETH Account 2 balance: " + ethAccount2.getAccountBalance().getSpendable().toString());

//             display HD account balance
            List<WalletAccount<?,?>> accounts = walletManager.getActiveAccounts();
            WalletAccount account = accounts.get(0);
            account.synchronize(SyncMode.NORMAL);
             System.out.println("HD Account balance: " + account.getAccountBalance().getSpendable().toString());
        } catch (TransactionBroadcastException ex) {
            ex.printStackTrace();
        } catch (WalletAccount.WalletAccountException ex) {
            ex.printStackTrace();
        } catch (KeyCipher.InvalidKeyCipher ex) {
            ex.printStackTrace();
        }

    }

    private static void sendColuWithFundingAccount(ColuAccount coluAccount, SingleAddressAccount coluSAAccount, HDAccount hdAccount1) {
        SendRequest<ColuTransaction> sendRequest = coluAccount.getSendToRequest(
                new BtcLegacyAddress(RMCCoin.INSTANCE
                        , Address.fromString("1MmgmNmKTzaNmQRi3DEmzULrxpPnxszh1c").getAllAddressBytes())
                , new Value(RMCCoin.INSTANCE, 1));
        if (sendRequest instanceof ColuSendRequest) {
            sendRequest.fee = Value.valueOf(coluSAAccount.getCoinType(), 10000000);
            List<BtcAddress> funding = new ArrayList<>();
            for (TransactionOutputSummary transactionOutputSummary : hdAccount1.getUnspentTransactionOutputSummary()) {
                funding.add(new BtcLegacyAddress(hdAccount1.getCoinType(), transactionOutputSummary.address.getAllAddressBytes()));
            }
            ((ColuSendRequest) sendRequest).setFundingAddress(funding);
        }
        coluAccount.completeTransaction(sendRequest);
        coluAccount.signTransaction(sendRequest);
    }

    private static void sendBtcFrom2Account(final NetworkParameters network, final HDAccount hdAccount1, final HDAccount hdAccount2, HDAccount hdAccount3) throws KeyCipher.InvalidKeyCipher {
        List<UnspentTransactionOutput> transform1 = transform(hdAccount1.getAccountBacking().getAllUnspentOutputs());
        List<UnspentTransactionOutput> transform2 = transform(hdAccount2.getAccountBacking().getAllUnspentOutputs());

        try {
            StandardTransactionBuilder stb = new StandardTransactionBuilder(network);

            WalletAccount.Receiver receiver = new WalletAccount.Receiver(hdAccount3.getReceiveAddress(), 10000l);

            stb.addOutput(((BtcAddress) receiver.address).getAddress(), receiver.amount);

            UnsignedTransaction unsignedTransaction = stb.createUnsignedTransaction(Arrays.asList(transform1.get(0), transform2.get(0))
                    , hdAccount1.getChangeAddress()
                    , new IPublicKeyRing() {
                        @Override
                        public PublicKey findPublicKeyByAddress(Address address) {
                            if (hdAccount1.isMine(address)) {
                                try {
                                    return hdAccount1.getPrivateKeyForAddress(address, AesKeyCipher.defaultKeyCipher()).getPublicKey();
                                } catch (KeyCipher.InvalidKeyCipher invalidKeyCipher) {
                                    invalidKeyCipher.printStackTrace();
                                }
                            } else if (hdAccount2.isMine(address)) {
                                try {
                                    return hdAccount2.getPrivateKeyForAddress(address, AesKeyCipher.defaultKeyCipher()).getPublicKey();
                                } catch (KeyCipher.InvalidKeyCipher invalidKeyCipher) {
                                    invalidKeyCipher.printStackTrace();
                                }
                            }
                            return null;
                        }
                    },
                    network, 1000);


            // Make all signatures, this is the CPU intensive part
            List<byte[]> signatures = StandardTransactionBuilder.generateSignatures(
                    unsignedTransaction.getSigningRequests(),
                    new IPrivateKeyRing() {
                        @Override
                        public BitcoinSigner findSignerByPublicKey(PublicKey publicKey) {
                            try {
                                InMemoryPrivateKey inMemoryPrivateKey1 = hdAccount1.getPrivateKey(publicKey, AesKeyCipher.defaultKeyCipher());
                                if (inMemoryPrivateKey1 != null) {
                                    return inMemoryPrivateKey1;
                                }
                                InMemoryPrivateKey inMemoryPrivateKey2 = hdAccount2.getPrivateKey(publicKey, AesKeyCipher.defaultKeyCipher());
                                if (inMemoryPrivateKey2 != null) {
                                    return inMemoryPrivateKey2;
                                }

                            } catch (KeyCipher.InvalidKeyCipher invalidKeyCipher) {
                                invalidKeyCipher.printStackTrace();
                            }
                            return null;
                        }
                    }
            );
            Transaction transaction = StandardTransactionBuilder.finalizeTransaction(unsignedTransaction, signatures);
            hdAccount1.broadcastTransaction(transaction);

        } catch (StandardTransactionBuilder.OutputTooSmallException e) {
            e.printStackTrace();
        } catch (StandardTransactionBuilder.InsufficientFundsException e) {
            e.printStackTrace();
        } catch (StandardTransactionBuilder.UnableToBuildTransactionException e) {
            e.printStackTrace();
        }
    }

    private static List<UnspentTransactionOutput> transform(Collection<TransactionOutputEx> source) {
        List<UnspentTransactionOutput> outputs = new ArrayList<>();
        for (TransactionOutputEx s : source) {
            ScriptOutput script = ScriptOutput.fromScriptBytes(s.script);
            outputs.add(new UnspentTransactionOutput(s.outPoint, s.height, s.value, script));
        }
        return outputs;
    }
}