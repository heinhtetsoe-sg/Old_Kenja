drop view V_SHUUGAKU_JYOUKYOU

create view V_SHUUGAKU_JYOUKYOU AS \
SELECT \
    substr(char(current date),1,7) as month, \
    t1.shuugaku_no, \
    case \
    when sougou_status_flg = '5' \
    then \
    '´°Ç¼' \
    when t4.chotei_kaisu is not null \
    then \
    'ÊÖ´ÔÃæ' \
    when t2.s_yuyo_ym is not null and t2.e_yuyo_ym is not null \
    then \
    'ÊÖ´ÔÍ±Í½Ãæ' \
    when t3.s_sueoki_ym is not null and t3.e_sueoki_ym is not null \
    then \
    '¿øÃÖ´ü´ÖÃæ' \
    when sougou_status_flg = '4' \
    then \
    'ÂÚÇ¼Ãæ' \
    when sougou_status_flg = '3' \
    then \
    'ÊÖ´Ô½àÈ÷Ãæ' \
    else \
    t5.name1 \
    end \
    as shuugaku_jyoukyou, \
    v1.shori_jyoukyou, \
    t1.sougou_status_flg, \
    t2.s_yuyo_ym, \
    t2.e_yuyo_ym, \
    t3.s_sueoki_ym, \
    t3.e_sueoki_ym, \
    t4.chotei_ym \
FROM \
    kojin_taiyo_dat t1 \
    left join v_kojin_shuugaku_shinsei_hist_dat v1 on t1.shuugaku_no = v1.shuugaku_no \
    left join yuyo_dat t2 on t1.shuugaku_no = t2.shuugaku_no and t2.upd_kubun = '1' and t2.uwagaki_flg = '0' and substr(char(current date),1,7) between t2.s_yuyo_ym and t2.e_yuyo_ym \
    left join yuyo_dat t3 on t1.shuugaku_no = t3.shuugaku_no and t3.upd_kubun = '1' and t3.uwagaki_flg = '0' and substr(char(current date),1,7) between t3.s_sueoki_ym and t3.e_sueoki_ym \
    left join chotei_dat t4 on t1.shuugaku_no = t4.shuugaku_no and substr(char(current date),1,7) = t4.chotei_ym  \
    left join name_mst t5 on v1.shori_jyoukyou = t5.namecd2 and t5.namecd1 = 'T042'
