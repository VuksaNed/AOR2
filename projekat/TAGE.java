/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.ac.bg.etf.vukasinovapredvidjanja;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

/**
 *
 * @author vukasin
 */
public class TAGE implements Predictor {

    class Tabela {

        int brojac = 0;
        int oznaka = -1;
        int u = 0;

    }

    Automaton automati[];
    int historisize;
    BHR bhr;
    int BHRsize;

    int brojacmax = 7;

    //5 4 3 1
    int mask[];
    int maskaa;

    Tabela tabele[][];

    public TAGE(int bhrsize, int tabsize, int maska, Automaton.AutomatonType tip) {
        bhr = new BHR(bhrsize);

        tabele = new Tabela[4][];
        for (int i = 0; i < 4; i++) {
            tabele[i] = new Tabela[tabsize];
            for (int j = 0; j < tabsize; j++) {
                tabele[i][j] = new Tabela();
            }
        }

        if (!((int) (Math.ceil((Math.log(tabsize) / Math.log(2)))) == (int) (Math.floor(((Math.log(tabsize) / Math.log(2))))))) {
            while ((!((int) (Math.ceil((Math.log(tabsize) / Math.log(2)))) == (int) (Math.floor(((Math.log(tabsize) / Math.log(2)))))))) {
                tabsize++;
            }
        }
        
        int velicina=maska;

        maskaa = (1<<15) - 1;
        mask = new int[5];
        mask[0] = (1 << 3) - 1;//3
        mask[1] = (1 << velicina) - 1;        //1
        mask[2] = (1 << (velicina + 2)) - 1;//3
        mask[3] = (1 << (velicina + 4)) - 1;//4
        mask[4] = (1 << (velicina + 6)) - 1;//5

        automati = Automaton.instanceArray(tip, (1 << 3));

    }

    private int daLiJeOznakaUtabeli(int oz, Tabela[] t) {
        for (int i = 0; i < t.length; i++) {
            if (oz == (t[i].oznaka)) {
                if (t[i].brojac >= brojacmax / 2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }

    private Tabela dohvatiTabelu(int oz, Tabela[] t) {
        for (int i = 0; i < t.length; i++) {
            if (oz == (t[i].oznaka)) {
                return t[i];
            }
        }
        return null;
    }

    @Override
    public boolean predict(Instruction branch) {
        int provajder = 5;
        int altprovajder = 5;
        int adresa = (int) branch.getAddress();
        for (int i = 0; i < 4; i++) {
            int oz = adresa;
            oz = oz ^ (bhr.getValue() & mask[1 + i]);
            int ret = daLiJeOznakaUtabeli(oz, tabele[i]);
            if (ret != -1) {
                if (provajder == 5) {
                    provajder = i;
                } else {
                    altprovajder = provajder;
                    provajder = i;
                }
            }
        }
        boolean ret;
        if (altprovajder == 5) {
            ret = automati[adresa & mask[0]].predict();
        } else {
            int oz = adresa;
            oz = oz ^ (bhr.getValue() & mask[1 + altprovajder]);
            ret = (dohvatiTabelu(oz, tabele[altprovajder]).brojac >= brojacmax / 2);
        }

        if (provajder == 5) {
            return ret;
        }

        int oz = adresa;
        oz = oz ^ (bhr.getValue() & mask[1 + provajder]);
        Tabela t = dohvatiTabelu(oz, tabele[provajder]);
        if ((t.u != 0) || (t.brojac != 3) || (t.brojac != 4)) {
            return t.brojac >= brojacmax / 2;
        } else {
            return ret;
        }

    }

    @Override
    public void update(Instruction branch) {
        int adresa = (int) branch.getAddress();
        boolean outcome = branch.isTaken();

        int provajder = 5;
        int altprovajder = 5;
        for (int i = 0; i < 4; i++) {
            int oz = adresa;
            oz = oz ^ (bhr.getValue() & mask[1 + i]);
            int ret = daLiJeOznakaUtabeli(oz, tabele[i]);
            if (ret != -1) {
                if (provajder == 5) {
                    provajder = i;
                } else {
                    altprovajder = provajder;
                    provajder = i;
                }
            }
        }
        boolean altret = false, predret = false;
        if (altprovajder == 5) {
            altprovajder = 0;
            altret = automati[adresa & mask[0]].predict();
        } else {
            int oz = adresa;
            oz = oz ^ (bhr.getValue() & mask[1 + altprovajder]);
            altret = (dohvatiTabelu(oz, tabele[altprovajder]).brojac >= brojacmax / 2);
        }
        boolean ret=altret;
        Tabela t=tabele[0][0];
        if (provajder != 5) {
            int oz = adresa;
            oz = oz ^ (bhr.getValue() & mask[1 + provajder]);
            t = dohvatiTabelu(oz, tabele[provajder]);     
            if ((t.u != 0) || (t.brojac != 3) || (t.brojac != 4)) {
                predret = t.brojac >= brojacmax / 2;
                ret = predret;
            } else {
                ret = altret;
            }
        }
//
        if (provajder < 5) {
            if (ret != altret) {
                if (ret == outcome) {
                    if (t.u < 7) {
                        t.u++;
                    }
                } else {
                    if (t.u > 0) {
                        t.u--;
                    }
                }
            }
            if (outcome) {
                if (t.brojac < brojacmax) {
                    t.brojac++;
                }
            } else {
                if (t.brojac > 0) {
                    t.brojac--;
                }
            }
        } else {
            automati[adresa & mask[0]].updateAutomaton(outcome);
        }

        boolean newentry = false;

        if (provajder < 5) {
            if ((t.u == 0) || (t.brojac == 3) || (t.brojac == 4)) {
                newentry = true;
            }
        }

        if ((!newentry) || (newentry && (outcome != predret))) {
            if ((outcome != ret)) {
                boolean ima = false;
                if (provajder==5) provajder=4;
                for (int i = 0; i < provajder; i++) {
                    for (int j = 0; j < tabele[i].length; j++) {
                        if (tabele[i][j].u == 0) {
                            ima = true;
                        }
                    }
                }
                if (!ima) {
                    for (int i = 0; i < provajder; i++) {
                        for (int j = 0; j < tabele[i].length; j++) {
                            if (tabele[i][j].u > 0) {
                                tabele[i][j].u--;
                            }
                        }
                    }
                } else {
                    int zamenai = 0, zamenaj = 0;
                    for (int i = 0; i < provajder; i++) {
                        for (int j = 0; j < tabele[i].length; j++) {
                            if (tabele[i][j].u == 0) {
                                zamenai = i;
                                zamenaj = j;
                            }
                        }
                    }
                    int tag = adresa & maskaa;
                    tag = tag ^ (bhr.getValue() & mask[1 + zamenai]);
                    tabele[zamenai][zamenaj].u = 0;
                    tabele[zamenai][zamenaj].oznaka = tag;
                    if (outcome) {
                        tabele[zamenai][zamenaj].brojac = 4;
                    } else {
                        tabele[zamenai][zamenaj].brojac = 3;
                    }
                }
            }
        }

        bhr.insertOutcome(outcome);

    }

}

/*


package rs.ac.bg.etf.vukasinovapredvidjanja;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;

public class YAGS implements Predictor {

    int selektor[];
    int historisize;

    BHR bhr;
    int mask;
    int maskzakes;
    int max;

    int kes[][];//////0- taken ///1-not taken
    int cnt[][];

    public YAGS(int bhrsize, int adressize, int adressizezakes, int kessize) {
        bhr = new BHR(bhrsize);
        selektor = new int[1 << adressize];
        mask = (1 << adressize) - 1;
        historisize = 4;
        max = kessize;
        maskzakes = (1 << adressizezakes )- 1;
        
        kes = new int[2][kessize];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < kessize; j++) {
                kes[i][j] = -1;
            }
        }

        cnt = new int[2][kessize];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < kessize; j++) {
                cnt[i][j] = -1;
            }
        }

    }

    private void AzurirajKesSaVrednoscu(int val, int ulaz) {
        int minndeks = -1;
        int min = this.max + 1;
        for (int i = 0; i < cnt[ulaz].length; i++) {
            if ((val==kes[ulaz][i])&&(cnt[ulaz][i]!=-1)){
                azurirajCeoKes(ulaz,i);
                return;
            }
            if (min > cnt[ulaz][i]) {
                min = cnt[ulaz][i];
                minndeks = i;
            }
        }
        kes[ulaz][minndeks] = val;
        cnt[ulaz][minndeks] = max;

        for (int i = 0; i < cnt[ulaz].length; i++) {
            if ((i != minndeks) && (cnt[ulaz][i] != -1)) {
                cnt[ulaz][i]--;
            }
        }

    }
    
    private void azurirajCeoKes(int x, int y){
        int staravrednost=cnt[x][y];
        cnt[x][y]=max+1;
        
        for (int i = 0; i < cnt[x].length; i++) {
            if (cnt[x][i]>staravrednost) cnt[x][i]--;
        }
    }

    private boolean nalaziSeUkesu(int val, int ulaz) {
        for (int i = 0; i < cnt[ulaz].length; i++) {
            if ((kes[ulaz][i] == val)&&(cnt[ulaz][i]!=-1)) {
                return true;
            }
        }
        return false;
    }

    private void izbaciIzKesa(int val, int ulaz){
        int staravrednost=0;
        for (int i = 0; i < cnt[ulaz].length; i++) {
            if ((val==kes[ulaz][i])&&(cnt[ulaz][i]!=-1)){
                staravrednost=cnt[ulaz][i];
                cnt[ulaz][i]=-1;
                break;
            }
        }
        
        for (int i = 0; i < cnt[ulaz].length; i++) {
            if ((staravrednost>cnt[ulaz][i])&&(cnt[ulaz][i]!=-1)){
               cnt[ulaz][i]++;
            }
        }
        
    }
    
    @Override
    public boolean predict(Instruction branch) {
        int index = (int) branch.getAddress() & mask;
        int indexs;
        if (selektor[index] >= 0) {
            indexs = 1;///1- predvidja taken
        } else {
            indexs = 0;//0-predvidja not taken
        }
        int zakes = (int) branch.getAddress() & maskzakes;
        zakes = zakes ^ (bhr.getValue() & maskzakes);

        if (nalaziSeUkesu(zakes, indexs)) {
            return (indexs != 1);
        } else {
            return (indexs == 1);
        }
    }

    @Override
    public void update(Instruction branch) {

        int index = (int) branch.getAddress() & mask;
        int indexs;
        if (selektor[index] >= 0) {
            indexs = 0;
        } else {
            indexs = 1;
        }

        int zakes = (int) branch.getAddress() & maskzakes;
        zakes = zakes ^ (bhr.getValue() & maskzakes);
        boolean out= branch.isTaken();
        if (nalaziSeUkesu(zakes, indexs)) {
            if ((indexs!=1)==out){
                AzurirajKesSaVrednoscu(zakes,indexs);
            }else{
                izbaciIzKesa(zakes,indexs);
                AzurirajKesSaVrednoscu(zakes,(indexs+1)%2);
            }
        } else {
            if (out){
                AzurirajKesSaVrednoscu(zakes,0);
            }else{
                AzurirajKesSaVrednoscu(zakes,1);
            }
        }
        
        bhr.insertOutcome(branch.isTaken());
        if (branch.isTaken()) {
            if (selektor[index] < historisize - 1) {
                selektor[index]++;
            }
        } else if (selektor[index] > -historisize) {
            selektor[index]--;
        }

    }

}



*/


//////////////////////

/*

package rs.ac.bg.etf.vukasinovapredvidjanja;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.predictor.BHR;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;



public class TAGE implements Predictor {

    class Tabela {

        Automaton brojac;
        int oznaka = -1;
        int u = 0;

        Tabela(Automaton.AutomatonType tip) {
            brojac = Automaton.instance(tip);
        }
    }

    Automaton automati[];
    int historisize;
    BHR bhr;
    int BHRsize;

    //5 4 3 1
    int mask[];
    int maskaa;

    Tabela tabele[][];

    public TAGE(int bhrsize, int tabsize, int maska, Automaton.AutomatonType tip) {
        bhr = new BHR(bhrsize);

        tabele = new Tabela[4][];
        for (int i = 0; i < 4; i++) {
            tabele[i] = new Tabela[tabsize];
            for (int j = 0; j < tabsize; j++) {
                tabele[i][j] = new Tabela(tip);
            }
        }
        
        if (!((int)(Math.ceil((Math.log(tabsize) / Math.log(2)))) == (int)(Math.floor(((Math.log(tabsize) / Math.log(2))))))){
            while ((!((int)(Math.ceil((Math.log(tabsize) / Math.log(2)))) == (int)(Math.floor(((Math.log(tabsize) / Math.log(2))))))))
                tabsize++;
        }

        maskaa=tabsize-1;
        mask = new int[5];
        mask[0] = (1 << 3) - 1;//3
        mask[1] = (1<<1)-1;        //1
        mask[2] = (1 << (2 + 1))-1;//3
        mask[3] = (1 << (1 + 3))-1;//4
        mask[4] = (1 << (0 + 5))-1;//5

        automati = Automaton.instanceArray(tip, (1 << 3));

    }

    private int daLiJeOznakaUtabeli(int oz, Tabela[] t) {
        for (int i = 0; i < t.length; i++) {
            if (oz == (t[i].oznaka)) {
                if (t[i].brojac.predict()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean predict(Instruction branch) {
        int adresa = (int) branch.getAddress();
        for (int i = 4; i > 0; i--) {
            int oz = adresa & maskaa;
            oz = oz ^ (bhr.getValue() & mask[i]);
            int ret = daLiJeOznakaUtabeli(oz, tabele[i - 1]);
            if (ret != -1) {
                return (ret == 1);
            }
        }

        int index = adresa & mask[0];

        return automati[index].predict();

    }

    @Override
    public void update(Instruction branch) {
        int adresa = (int) branch.getAddress();
        boolean outcome = branch.isTaken();

        for (int i = 4; i > 0; i--) {
            int oz = adresa & maskaa;
            oz = oz ^ (bhr.getValue() & mask[i]);
            int ret = daLiJeOznakaUtabeli(oz, tabele[i - 1]);
            if (ret != -1) {
                if (outcome == (ret == 1)) {
                    for (int j = 0; j < tabele[i - 1].length; j++) {
                        if (oz == tabele[i - 1][j].oznaka) {
                            tabele[i - 1][j].brojac.updateAutomaton(outcome);
                            tabele[i - 1][j].u += 1;
                            bhr.insertOutcome(branch.isTaken());
                            return;
                        }
                    }
                } else {
                    for (int j = 0; j < tabele[i - 1].length; j++) {
                        if (oz == tabele[i - 1][j].oznaka) {
                            tabele[i - 1][j].brojac.updateAutomaton(outcome);
                            if (tabele[i - 1][j].u != 0) {
                                tabele[i - 1][j].u += -1;
                            }
                            bhr.insertOutcome(branch.isTaken());
                            //

                            for (int k = 4; k < i; k--) {
                                for (int p = 0; p < tabele[k - 1].length; p++) {
                                    if (tabele[k - 1][p].u == 0) {
                                        tabele[k - 1][p].u = 1;
                                        tabele[k - 1][p].brojac.updateAutomaton(outcome);
                                        oz = adresa & maskaa;
                                        oz = oz ^ (bhr.getValue() & mask[k]);
                                        tabele[k - 1][p].oznaka = oz;
                                        return;
                                    }
                                }
                            }
                            for (int k = 4; k > 0; k--) {
                                for (int p = 0; p < tabele[k - 1].length; p++) {
                                    if (tabele[k - 1][p].u != 0) {
                                        tabele[k - 1][p].u += -1;
                                    }
                                }
                            }

                            //
                            return;
                        }
                    }
                }
            }
        }

        //ako nema
        int index = (int) branch.getAddress() & mask[0];

        automati[(bhr.getValue() ^ index) & mask[0]].updateAutomaton(branch.isTaken());

        for (int k = 4; k > 0; k--) {
            for (int p = 0; p < tabele[k - 1].length; p++) {
                if (tabele[k - 1][p].u == 0) {
                    tabele[k - 1][p].u = 0;
                    tabele[k - 1][p].brojac.updateAutomaton(outcome);
                    int oz = adresa & maskaa;
                    oz = oz ^ (bhr.getValue() & mask[k]);
                    tabele[k - 1][p].oznaka = oz;
                    bhr.insertOutcome(outcome);
                    return;
                }
            }
        }
        for (int k = 4; k > 0; k--) {
            for (int p = 0; p < tabele[k - 1].length; p++) {
                if (tabele[k - 1][p].u != 0) {
                    tabele[k - 1][p].u += -1;
                }
            }
        }
        bhr.insertOutcome(outcome);

    }

}



*/




