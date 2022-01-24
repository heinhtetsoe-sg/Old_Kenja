<?php

require_once('for_php7.php');

class knjz021Model extends Model {
    var $cmd;
    var $field = array();
    var $year;
    var $virus;

    function knjz021Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        if (VARS::request("year")!="") {
            $this->year = VARS::request("year"); //年度コンボ
        } else {
            $this->year = CTRL_YEAR; //初期値
        }
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::request("SENDSCHKIND") != "") {
                $this->sendSchkind = VARS::request("SENDSCHKIND"); //校種受け取り
            }
        }
        $this->field = array("JUGYOU_JISU_FLG"         => VARS::post("JUGYOU_JISU_FLG"),
                             "RISYU_BUNSI"             => VARS::post("RISYU_BUNSI"),
                             "RISYU_BUNBO"             => VARS::post("RISYU_BUNBO"),
                             "SYUTOKU_BUNSI"           => VARS::post("SYUTOKU_BUNSI"),
                             "SYUTOKU_BUNBO"           => VARS::post("SYUTOKU_BUNBO"),
                             "RISYU_BUNSI_SPECIAL"     => VARS::post("RISYU_BUNSI_SPECIAL"),
                             "RISYU_BUNBO_SPECIAL"     => VARS::post("RISYU_BUNBO_SPECIAL"),
                             "JOUGENTI_SANSYUTU_HOU"   => VARS::post("JOUGENTI_SANSYUTU_HOU"),
                             "JITU_JIFUN"              => VARS::post("JITU_JIFUN"),
                             "JITU_JIFUN_SPECIAL"      => VARS::post("JITU_JIFUN_SPECIAL"),
                             "JITU_SYUSU"              => VARS::post("JITU_SYUSU"),
                             "KESSEKI_WARN_BUNSI"      => VARS::post("KESSEKI_WARN_BUNSI"),
                             "KESSEKI_WARN_BUNBO"      => VARS::post("KESSEKI_WARN_BUNBO"),
                             "KESSEKI_OUT_BUNSI"       => VARS::post("KESSEKI_OUT_BUNSI"),
                             "KESSEKI_OUT_BUNBO"       => VARS::post("KESSEKI_OUT_BUNBO"),
                             "PREF_CD"                 => VARS::post("PREF_CD"),
                             "TOKUBETU_KATUDO_KANSAN"  => VARS::post("TOKUBETU_KATUDO_KANSAN"),
                             "AMARI_KURIAGE"           => VARS::post("AMARI_KURIAGE"),
                             "KYOUIKU_IINKAI_SCHOOLCD" => VARS::post("KYOUIKU_IINKAI_SCHOOLCD"),
                             "PARTS_HYOUKA_HYOUTEI_KEISAN" => VARS::post("PARTS_HYOUKA_HYOUTEI_KEISAN"),
                             "MAX_SEMESTER"            => VARS::post("MAX_SEMESTER")
                            );

        if ($this->field["MAX_SEMESTER"]) {
            for ($sem = 1; $sem <= $this->field["MAX_SEMESTER"]; $sem++) {
                $this->field["HOUTEI_SYUSU_SEMESTER".$sem] =  VARS::post("HOUTEI_SYUSU_SEMESTER".$sem);
            }
        }

        $db = Query::dbCheckOut();
        //月別授業週数
        $this->seminfo = array();
        $query = knjz021Query::selectSemesAll($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->seminfo[] = $row;

            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = $i;
                if ($i > 12) $month = $i - 12;
                $month = sprintf('%02d', $month);
                $this->field["HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."-".$month] =  VARS::post("HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."-".$month);
            }
        }
        $result->free();

        $query = knjz021Query::getDataBaseInfo();
        $this->dataBaseinfo = $db->getOne($query); //教育委員会情報
        Query::dbCheckIn($db);
        
    }

    function validateInput() {
        if ($this->field["JUGYOU_JISU_FLG"] === '1' && $this->Properties["hibiNyuuryokuNasi"] === '1') {
            $syusu_sum = 0;
            $syusu_msg = "";

            if ($this->Properties["use_Month_Syusu"] == "1") {
                for ($dcnt = 0; $dcnt < get_count($this->seminfo); $dcnt++) {
                    for ($i = $this->seminfo[$dcnt]["S_MONTH"]; $i <= $this->seminfo[$dcnt]["E_MONTH"]; $i++) {
                        $month = $i;
                        if ($i > 12) $month = $i - 12;
                        $month = sprintf('%02d', $month);
                        //各月の週数を加算
                        $syusu_sum += $this->field["HOUTEI_SYUSU_MONTH_".$this->seminfo[$dcnt]["SEMESTER"]."-".$month];
                    }
                }
                $syusu_msg = "月";
            } else {
                //各学期の週数を加算
                for ($sem = 1; $sem <= $this->field["MAX_SEMESTER"]; $sem++) {
                    $syusu_sum += intval($this->field["HOUTEI_SYUSU_SEMESTER".$sem]);
                }
                $syusu_msg = "学期";
            }
            //値チェック
            if (intval($this->field["JITU_SYUSU"]) !== $syusu_sum) {
                $this->setWarning("MSG901","各{$syusu_msg}の週数の合計が授業週数と一致していません。");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz021Query::updateSchoolDetailDat($this);
            $this->setMessage("MSG201");
        }
    }

    function &update() {}

    function clean() {}
}
?>
