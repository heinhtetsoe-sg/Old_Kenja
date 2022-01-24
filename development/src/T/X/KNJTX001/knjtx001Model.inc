<?php

require_once('for_php7.php');

class knjtx001Model extends Model {
    var $cmd;
    var $field = array();
    var $year;
    var $virus;

    function knjtx001Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        if (VARS::post("year")!="") {
            $this->year = VARS::post("year"); //年度コンボ
        } else {
            $this->year = CTRL_YEAR; //初期値
        }

        $this->field = array("FOUNDEDYEAR"            => VARS::post("FOUNDEDYEAR"),
                             "PRESENT_EST"            => VARS::post("PRESENT_EST"),
                             "CLASSIFICATION"         => VARS::post("CLASSIFICATION"),
                             "SCHOOLNAME1"            => VARS::post("SCHOOLNAME1"),
                             "SCHOOLNAME2"            => VARS::post("SCHOOLNAME2"),
                             "SCHOOLNAME3"            => VARS::post("SCHOOLNAME3"),
                             "SCHOOLNAME_ENG"         => VARS::post("SCHOOLNAME_ENG"),
                             "SCHOOLZIPCD"            => VARS::post("SCHOOLZIPCD"),
                             "SCHOOLADDR1"            => VARS::post("SCHOOLADDR1"),
                             "SCHOOLADDR2"            => VARS::post("SCHOOLADDR2"),
                             "SCHOOLADDR1_ENG"        => VARS::post("SCHOOLADDR1_ENG"),
                             "SCHOOLADDR2_ENG"        => VARS::post("SCHOOLADDR2_ENG"),
                             "SCHOOLTELNO"            => VARS::post("SCHOOLTELNO"),
                             "SCHOOLFAXNO"            => VARS::post("SCHOOLFAXNO"),
                             "SCHOOLMAIL"             => VARS::post("SCHOOLMAIL"),
                             "SCHOOLURL"              => VARS::post("SCHOOLURL"),
                             "SCHOOLDIV"              => VARS::post("SCHOOLDIV"),
                             "SEMESTERDIV"            => VARS::post("SEMESTERDIV"),
                             "GRADE_HVAL"             => VARS::post("GRADE_HVAL"),
                             "ENTRANCE_DATE"          => VARS::post("ENTRANCE_DATE"),
                             "GRADUATE_DATE"          => VARS::post("GRADUATE_DATE"),
                             "GRAD_CREDITS"           => VARS::post("GRAD_CREDITS"),
                             "GRAD_COMP_CREDITS"      => VARS::post("GRAD_COMP_CREDITS"),
                             "SEMES_ASSESSCD"         => VARS::post("SEMES_ASSESSCD"),
                             "SEMES_FEARVAL"          => VARS::post("SEMES_FEARVAL"),
                             "GRADE_FEARVAL"          => VARS::post("GRADE_FEARVAL"),
                             "ABSENT_COV"             => VARS::post("ABSENT_COV"),
                             "ABSENT_COV_LATE"        => VARS::post("ABSENT_COV_LATE"),
                             "AMARI_KURIAGE"          => VARS::post("AMARI_KURIAGE"),
                             "GVAL_CALC"              => VARS::post("GVAL_CALC"),
                             "SEM_OFFDAYS"            => VARS::post("SEM_OFFDAYS"),
                             "SUB_OFFDAYS"            => VARS::post("SUB_OFFDAYS"),
                             "SUB_MOURNING"           => VARS::post("SUB_MOURNING"),
                             "SUB_SUSPEND"            => VARS::post("SUB_SUSPEND"),
                             "SUB_VIRUS"              => VARS::post("SUB_VIRUS"),
                             "SUB_ABSENT"             => VARS::post("SUB_ABSENT"),
                             "TOKUBETU_KATUDO_KANSAN" => VARS::post("TOKUBETU_KATUDO_KANSAN"),
                             "SYUKESSEKI_HANTEI_HOU"  => VARS::post("SYUKESSEKI_HANTEI_HOU"),
                             "UPDATED"                => VARS::post("UPDATED"));

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? $this->Properties["useVirus"] : false;
    }

    function validateInput() {
        //必須入力項目チェック
        if ($this->field["FOUNDEDYEAR"]==""  || $this->field["PRESENT_EST"]=="" || $this->field["CLASSIFICATION"]=="" ||
            $this->field["SCHOOLNAME1"]==""  || $this->field["SCHOOLDIV"]==""   || $this->field["SEMESTERDIV"]=="" ||
            $this->field["GRADE_HVAL"]==""   || $this->field["SEMES_ASSESSCD"]=="") {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["GVAL_CALC"]=="") {
            $this->setWarning("MSG310","(評定計算方法)");
            return false;
        }

        if ($this->field["FOUNDEDYEAR"] < 1) {
            $this->setWarning("MSG916","(創立年度)");
            return false;
        }
        if ($this->field["PRESENT_EST"] < 1) {
            $this->setWarning("MSG916","(現在期)");
            return false;
        }

        foreach (array("CLASSIFICATION" => "(学校種別)",
                       "SCHOOLNAME1"    => "(学校名称1)",
                       "SCHOOLNAME2"    => "(学校名称2)",
                       "SCHOOLNAME3"    => "(学校名称3)",
                       "SCHOOLADDR1"    => "(住所１)",
                       "SCHOOLADDR2"    => "(住所２)") as $key => $val) {

            if ($key == "SCHOOLADDR1" || $key == "SCHOOLADDR2") {
                if (mbereg("[｡-ﾟ]", $this->field[$key])) { //半角カナ禁止
                    $this->setWarning("MSG904",$val); return false;
                }
            } elseif (trim($this->field[$key]) != i18n_ja_jp_hantozen(trim($this->field[$key]),"AKHV")) {
                $this->setWarning("MSG904",$val); return false;
            }
        }

        foreach (array("SCHOOLNAME_ENG"  => "(学校名称英字)",
                       "SCHOOLADDR1_ENG" => "(住所１(英字))",
                       "SCHOOLADDR2_ENG" => "(住所２(英字))",
                       "SCHOOLMAIL"      => "(メールアドレス)",
                       "SCHOOLURL"       => "(ホームページ)") as $key => $val) {

            if (!preg_match("/^[ A-z0-9 -~]*$/",$this->field[$key])) {
                $this->setWarning("MSG905",$val);
                return false;
            }
        }

        if (strlen($this->field["SCHOOLTELNO"]) && !preg_match("/^[-0-9]*$/", $this->field["SCHOOLTELNO"])) {
            $this->setWarning("MSG909","(電話番号)");
            return false;
        }

        if (strlen($this->field["SCHOOLFAXNO"]) && !preg_match("/^[-0-9]*$/", $this->field["SCHOOLFAXNO"])) {
            $this->setWarning("MSG909","(ＦＡＸ番号)");
            return false;
        }

        if ($this->field["SEMESTERDIV"] < 1 || $this->field["SEMESTERDIV"] > 8 ) {
            $this->setWarning("MSG916","(学期制)");
            return false;
        }

        if ($this->field["GRADE_HVAL"] < 1) {
            $this->setWarning("MSG916","(学年数上限)　1以上を指定してください");
            return false;
        }

        //バイト数チェック
        foreach (array("CLASSIFICATION"  => array( 6,"(学校種別)"),
                       "SCHOOLNAME1"     => array(90,"(学校名称１)"),
                       "SCHOOLNAME2"     => array(90,"(学校名称２)"),
                       "SCHOOLNAME3"     => array(90,"(学校名称３)"),
                       "SCHOOLNAME_ENG"  => array(60,"(学校名称英字)"),
                       "SCHOOLZIPCD"     => array( 8,"(郵便番号)"),
                       "SCHOOLADDR1"     => array(75,"(住所１)"),
                       "SCHOOLADDR2"     => array(75,"(住所２)"),
                       "SCHOOLADDR1_ENG" => array(50,"(住所１(英字))"),
                       "SCHOOLADDR2_ENG" => array(50,"(住所２(英字))"),
                       "SCHOOLTELNO"     => array(14,"(電話番号)"),
                       "SCHOOLFAXNO"     => array(14,"(ＦＡＸ番号)"),
                       "SCHOOLMAIL"      => array(25,"(メールアドレス)"),
                       "SCHOOLURL"       => array(30,"(ホームページ)")) as $key => $val) {
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG915",$val[1]); return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {

            if ($this->field["SEMESTERDIV"] != "") {
                knjtx001Query::updateSemesterMst($this->field["SEMESTERDIV"],$this->year);
            }
            knjtx001Query::updateSchoolMst($this);
            $this->setMessage("MSG201");
        }
    }

    function &update() {}

    function clean() {}
}
?>
