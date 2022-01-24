<?php

require_once('for_php7.php');

class knjz025Model extends Model {
    var $cmd;
    var $field = array();
    var $year;

    function knjz025Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        //初期値セット
        $this->year = (VARS::post("year") != "") ? VARS::post("year") : CTRL_YEAR;

        $this->field = array("PREF_CD"              => VARS::post("PREF_CD"),
                             "SCHOOLNAME1"          => VARS::post("SCHOOLNAME1"),
                             "SCHOOLNAME2"          => VARS::post("SCHOOLNAME2"),
                             "SCHOOLNAME3"          => VARS::post("SCHOOLNAME3"),
                             "SCHOOLNAME_ENG"       => VARS::post("SCHOOLNAME_ENG"),
                             "SCHOOLZIPCD"          => VARS::post("SCHOOLZIPCD"),
                             "SCHOOLADDR1"          => VARS::post("SCHOOLADDR1"),
                             "SCHOOLADDR2"          => VARS::post("SCHOOLADDR2"),
                             "SCHOOLADDR1_ENG"      => VARS::post("SCHOOLADDR1_ENG"),
                             "SCHOOLADDR2_ENG"      => VARS::post("SCHOOLADDR2_ENG"),
                             "SCHOOLTELNO"          => VARS::post("SCHOOLTELNO"),
                             "SCHOOLFAXNO"          => VARS::post("SCHOOLFAXNO"),
                             "SCHOOLMAIL"           => VARS::post("SCHOOLMAIL"),
                             "SCHOOLURL"            => VARS::post("SCHOOLURL"),
                             "UPDATED"              => VARS::post("UPDATED"));

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput() {
        //必須入力項目チェック
        if ($this->field["SCHOOLNAME1"] == "") {
            $this->setWarning("MSG301");
            return false;
        }

        //入力文字チェック
        foreach (array("SCHOOLNAME1"    => "(教育委員会名称1)",
                       "SCHOOLNAME2"    => "(教育委員会名称2)",
                       "SCHOOLNAME3"    => "(教育委員会名称3)",
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
        foreach (array("SCHOOLNAME_ENG"  => "(教育委員会名称英字)",
                       "SCHOOLADDR1_ENG" => "(住所１(英字))",
                       "SCHOOLADDR2_ENG" => "(住所２(英字))",
                       "SCHOOLMAIL"      => "(メールアドレス)",
                       "SCHOOLURL"       => "(ホームページ)") as $key => $val) {

            if (!preg_match("/^[ A-z0-9 -~]*$/",$this->field[$key])) {
                $this->setWarning("MSG905",$val);
                return false;
            }
        }
        if (strlen($this->field["SCHOOLTELNO"]) && !preg_match(/"^[-0-9]*$/", $this->field["SCHOOLTELNO"])) {
            $this->setWarning("MSG909","(電話番号)");
            return false;
        }
        if (strlen($this->field["SCHOOLFAXNO"]) && !preg_match("/^[-0-9]*$/", $this->field["SCHOOLFAXNO"])) {
            $this->setWarning("MSG909","(ＦＡＸ番号)");
            return false;
        }

        //バイト数チェック
        foreach (array("SCHOOLNAME1"     => array(90,"(教育委員会名称１)"),
                       "SCHOOLNAME2"     => array(90,"(教育委員会名称２)"),
                       "SCHOOLNAME3"     => array(90,"(教育委員会名称３)"),
                       "SCHOOLNAME_ENG"  => array(60,"(教育委員会名称英字)"),
                       "SCHOOLZIPCD"     => array( 8,"(郵便番号)"),
                       "SCHOOLADDR1"     => array(90,"(住所１)"),
                       "SCHOOLADDR2"     => array(90,"(住所２)"),
                       "SCHOOLADDR1_ENG" => array(70,"(住所１(英字))"),
                       "SCHOOLADDR2_ENG" => array(70,"(住所２(英字))"),
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

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            //学期マスタ更新
            knjz025Query::updateSemesterMst($this->year);
            knjz025Query::updateSchoolMst($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
