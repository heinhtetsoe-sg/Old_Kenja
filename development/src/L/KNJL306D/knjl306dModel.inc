<?php

require_once('for_php7.php');

class knjl306dModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjl306dModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("ASSESSCD"         =>  VARS::post("ASSESSCD"),         //評定区分
                             "MAX_ASSESSLEVEL"  =>  VARS::post("MAX_ASSESSLEVEL"),  //段階数
                             "ASSESSHIGH_MAX"   =>  VARS::post("ASSESSHIGH_MAX"),   //上限テキスト
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["ROW_NUM"]); $i++) { //ROW_NUMは段階値数
            //下限値
            $assesslow = "ASSESSLOW"."-".$i;
            $this->fields["ASSESSLOW"][]  = VARS::post($assesslow);
            //記号
            $assessmark  = "ASSESSMARK"."-".$i;
            $this->fields["ASSESSMARK"][]  = VARS::post($assessmark);

            //段階値の上限値をhiddenで保持
            $num = $i+1;
            $assesshigh = "Assesshighvalue".$num;
            $this->fields["ASSESSHIGH"][] = VARS::request($assesshigh);
        }

        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->ObjYear + 1;
        $message = '';
        $flg = true;
        $chkCnt1 = $db->getOne(knjl306dQuery::getCheckEntexamAssessMstSql($this->ObjYear));
        if ($chkCnt1 == 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        $chkCnt2 = $db->getOne(knjl306dQuery::getCheckEntexamAssessMstSql($this->nextYear));
        if ($chkCnt2 != 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl306dQuery::getCopyQuery($this->ObjYear, $this->nextYear);
            $db->query($query);
            $query = knjl306dQuery::getCopyQuery2($this->ObjYear, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["ASSESSCD"] == "") {
            $this->setWarning("MSG304", "( 評定区分 )");
            return false;
        }

        //段階値得点の前後チェック
        for ($i = 0; $i < get_count($this->data["ROW_NUM"]); $i++) {
            //下限値チェック
            if ($this->fields["ASSESSLOW"][$i] == "") {
                $this->setWarning("MSG901","(下限値が未入力の箇所があります。)");
                return false;
            }
            //重複チェック
            for ($k = 0; $k < get_count($this->data["ROW_NUM"]); $k++) {
                if ($i != $k) {
                    if ($this->fields["ASSESSLOW"][$i] == $this->fields["ASSESSLOW"][$k]) {
                        $this->setWarning("MSG901","(下限値が重複しています。)");
                        return false;
                    }
                }
            }
            //記号チェック
            if (strlen($this->fields["ASSESSMARK"][$i]) > 6) {
                $this->setWarning("MSG901","(記号は6バイトまでです)");
                return false;
            }
            //範囲チェック
            if ($i+1 < get_count($this->data["ROW_NUM"])) {
                if ($this->fields["ASSESSLOW"][$i] > $this->fields["ASSESSLOW"][$i+1]) {
                    $this->setWarning("MSG901","(下限値の範囲が不正です)");
                    return false;
                }
            }
            if ($this->field["ASSESSHIGH_MAX"] < $this->fields["ASSESSLOW"][get_count($this->data["ROW_NUM"]) -1]) {
                $this->setWarning("MSG901","(下限値の値が上限値MAXを超えています)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjl306dQuery::delete($this, $db);
            knjl306dQuery::update($this, $db);
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = get_count($this->data["ROW_NUM"]);
        }
        Query::dbCheckIn($db);
        return true;
    }
}
//var_dump($_REQUEST);
?>
