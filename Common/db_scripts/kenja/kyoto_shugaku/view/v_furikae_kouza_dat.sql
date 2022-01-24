drop view V_FURIKAE_KOUZA_DAT

create view V_FURIKAE_KOUZA_DAT AS \
SELECT  \
    X1.*, \
    H1.bankname, \
    H1.branchname, \
    H1.bankname_kana, \
    H1.branchname_kana \
FROM  \
(  \
SELECT  \
    v1.shuugaku_no,  \
    v1.chotei_kaisu,  \
    v1.chotei_ym,  \
    v1.torikesi_flg,  \
    v1.chotei_date,  \
    v1.shiharai_hoho_cd,  \
    v1.henkan_gk,  \
    v2.kojin_no,  \
    v2.shiharainin_kbn,  \
    v2.shiharai_hoho,  \
    v3.shikin_shousai_div,  \
    t3.abbv3,  \
    case when v2.shiharainin_kbn = '2' then t2.bankcd else t1.bankcd end as bankcd, \
    case when v2.shiharainin_kbn = '2' then t2.branchcd else t1.branchcd end as branchcd,  \
    case when v2.shiharainin_kbn = '2' then t2.yokin_div else t1.yokin_div end as yokin_div,  \
    case when v2.shiharainin_kbn = '2' then t2.account_no else t1.account_no end as account_no,  \
    case when v2.shiharainin_kbn = '2' then t2.bank_meigi_sei_kana else t1.bank_meigi_sei_kana end as bank_meigi_sei_kana,  \
    case when v2.shiharainin_kbn = '2' then t2.bank_meigi_mei_kana else t1.bank_meigi_mei_kana end as bank_meigi_mei_kana  \
FROM  \
   v_chotei_noufu v1  \
   left join v_saiken_jokyo v2 on v1.shuugaku_no = v2.shuugaku_no  \
   left join v_kojin_shuugaku_shinsei_hist_dat v3 on v1.shuugaku_no = v3.shuugaku_no  \
   left join (select kojin_no,max(s_date) as s_date from kojin_kouza_bank_dat where taishousha_div = '1' and kouza_div = '2' group by kojin_no,kouza_div) as w1 on v2.kojin_no = w1.kojin_no  \
   left join (select kojin_no,max(s_date) as s_date from kojin_kouza_bank_dat where taishousha_div = '3' and kouza_div = '2' group by kojin_no,kouza_div) as w2 on v3.rentai_cd = w2.kojin_no  \
   left join kojin_kouza_bank_dat t1 on v2.kojin_no = t1.kojin_no and t1.taishousha_div = '1' and t1.kouza_div = '2' and w1.s_date = t1.s_date  \
   left join kojin_kouza_bank_dat t2 on v3.rentai_cd = t2.kojin_no and t2.taishousha_div = '3' and t2.kouza_div = '2' and w2.s_date = t2.s_date  \
   left join name_mst t3 on v3.shikin_shousai_div = t3.namecd2 and namecd1 = 'T030'  \
WHERE  \
    v1.shiharai_hoho_cd = '1'  \
and  \
    torikesi_flg = '0'  \
and  \
    haito_flg = '0'  \
and  \
    bunkatu_flg = '0'  \
) as X1  \
   LEFT JOIN BANK_MST H1 on X1.BANKCD = H1.BANKCD AND X1.BRANCHCD = H1.BRANCHCD 
