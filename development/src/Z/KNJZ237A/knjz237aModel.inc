<?php

require_once('for_php7.php');

class knjz237aModel extends Model {
    var $field = array();
    var $cmd;
    var $staffcd;
    var $control;

    function knjz237aModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::request("cmd") == '') {
            unset($this->semester);
            unset($this->testkindcd);
        }
        if (VARS::post("cmd") == "list") {
            unset($this->semester);
            unset($this->testkindcd);
        }

        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        
        $this->field = array("SEMESTER"         => VARS::post("SEMESTER"),
                             "TESTKINDCD"       => VARS::post("TESTKINDCD"),
                             "SUBCLASSCD"       => VARS::post("SUBCLASSCD"),
                             "DIV"              => sprintf('%02d', VARS::post("DIV")),
                             "GRADE"            => VARS::post("GRADE"),
                             "COURSECD"         => VARS::post("COURSECD"),
                             "MAJORCD"          => VARS::post("MAJORCD"),
                             "COURSECODE"       => VARS::post("COURSECODE"),
                             "PERFECT"          => VARS::post("PERFECT"),
                             "PASS_SCORE"       => VARS::post("PASS_SCORE")
                             );

        if (VARS::get("cmd") == "edit"){
            $this->semester                 = VARS::get("SEMESTER");
            $this->testkindcd               = VARS::get("TESTKINDCD");
            $this->subclasscd               = VARS::get("SUBCLASSCD");
            $this->div                      = VARS::get("DIV");
            $this->grade                    = VARS::get("GRADE");
            $this->coursecd                 = VARS::get("COURSECD");
            $this->majorcd                  = VARS::get("MAJORCD");
            $this->coursecode               = VARS::get("COURSECODE");
            $this->perfect                  = VARS::get("PERFECT");
            $this->pass_score               = VARS::get("PASS_SCORE");
        }

        if (VARS::get("cmd") == "edit" || $this->cmd == "reset"){
            $this->field["SEMESTER"]                = $this->semester;
            $this->field["TESTKINDCD"]              = $this->testkindcd;
            $this->field["SUBCLASSCD"]              = $this->subclasscd;
            $this->field["DIV"]                     = $this->div;
            $this->field["GRADE"]                   = $this->grade;
            $this->field["COURSECD"]                = $this->coursecd;
            $this->field["MAJORCD"]                 = $this->majorcd;
            $this->field["COURSECODE"]              = $this->coursecode;
            $this->field["PERFECT"]                 = $this->perfect;
            $this->field["PASS_SCORE"]              = $this->pass_score;
        }

        if (VARS::get("cmd") == "list_update") {
            $this->field["SEMESTER"]    = $this->semester;
            $this->field["TESTKINDCD"]  = $this->testkindcd;
        }

        if (VARS::request("SEMESTER")) {
            $this->semester     = VARS::request("SEMESTER");
            $this->testkindcd   = VARS::request("TESTKINDCD");
        }

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }
    }

    //入力項目チェック
    function validateInput() {
        //入力チェック
        if (trim($this->field["SEMESTER"]) == "") {
            $this->setWarning('MSG301', "(学期)");
            return false;
        }
        if (trim($this->field["TESTKINDCD"]) == "") {
            $this->setWarning('MSG301', "(考査)");
            return false;
        }
        if (trim($this->field["SUBCLASSCD"]) == "") {
            $this->setWarning('MSG301', "(科目)");
            return false;
        }
        if (trim($this->field["DIV"]) == "") {
            $this->setWarning('MSG301', "(区分)");
            return false;
        }
        if ($this->field["DIV"] == "02" || $this->field["DIV"] == "03" || $this->field["DIV"] == "04") {
            if(trim($this->field["GRADE"]) == "") {
                $this->setWarning('MSG301', "(学年)");
                return false;
            }
            if ($this->field["DIV"] == "03") {
                if(trim($this->field["COURSECD"]) == "") {
                    $this->setWarning('MSG301', "(課程)");
                    return false;
                }
                if(trim($this->field["MAJORCD"]) == "") {
                    $this->setWarning('MSG301', "(学科)");
                    return false;
                }
                if(trim($this->field["COURSECODE"]) == "") {
                    $this->setWarning('MSG301', "(コース)");
                    return false;
                }
            } else if ($this->field["DIV"] == "04") {
                if(trim($this->field["MAJORCD"]) == "") {
                    $this->setWarning('MSG301', "(グループコード)");
                    return false;
                }
                $this->field["COURSECD"]   = "0";
                $this->field["COURSECODE"] = "0000";
            } else {
                $this->field["COURSECD"]   = "0";
                $this->field["MAJORCD"]    = "000";
                $this->field["COURSECODE"] = "0000";
            }
        } else {
            $this->field["GRADE"]      = "00";
            $this->field["COURSECD"]   = "0";
            $this->field["MAJORCD"]    = "000";
            $this->field["COURSECODE"] = "0000";
        }
        if (strlen($this->field["PERFECT"]) > 3 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(文字数が不正です。)");
            return false;
        }
        //update,delete共通のチェック
        if($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->semester != $this->field["SEMESTER"]) {
                $this->setWarning('MSG308',"(学期)");
                return false;
            } else if ($this->testkindcd != $this->field["TESTKINDCD"]) {
                $this->setWarning('MSG308',"(考査)");
                return false;
            } else if ($this->subclasscd != $this->field["SUBCLASSCD"]) {
                $this->setWarning('MSG308',"(科目)");
                return false;
            } else if ($this->div != $this->field["DIV"]) {
                $this->setWarning('MSG308',"(区分)");
                return false;
            } else if ($this->div !== $this->field["DIV"]) {
                if($this->cmd == "update"){
                    $this->setWarning('データが不正です。','更新に失敗しました。');
                    return false;
                } else {
                    $this->setWarning('MSG919');
                    return false;
                }
            } else if ($this->grade != $this->field["GRADE"]) {
                $this->setWarning('MSG308',"(学年)");
                return false;
            } else if ($this->coursecd != $this->field["COURSECD"]) {
                $this->setWarning('MSG308',"(課程)");
                return false;
            } else if (($this->majorcd != $this->field["MAJORCD"]) &&  ($this->field["DIV"] == '04')) {
                $this->setWarning('MSG308',"(グループコード)");
                return false;
            } else if ($this->majorcd != $this->field["MAJORCD"]) {
                $this->setWarning('MSG308',"(学科)");
                return false;
            } else if ($this->coursecode != $this->field["COURSECODE"]) {
                $this->setWarning('MSG308',"(コース)");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            $db = Query::dbCheckOut();
            $query = knjz237aQuery::getExists($this);
            $ret_val = $db->getOne($query);
            if (!$ret_val) {
                $this->setWarning('MSG303');
                return false;
            }
            Query::dbCheckIn($db);
        }

        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $cnt = knjz237aQuery::getCopyCountQuery($this);
        if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjz237aQuery::getCopyQuery($this);
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->setMessage('MSG201');
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage('MSG201');
        }
    }

    //インサート時の内部メソッド
    function &insert() {
        knjz237aQuery::getInsertUpdateQuery($this);
        $this->semester             = $this->field["SEMESTER"];
        $this->testkindcd           = $this->field["TESTKINDCD"];
        $this->subclasscd           = $this->field["SUBCLASSCD"];
        $this->div                  = $this->field["DIV"];
        $this->grade                = $this->field["GRADE"];
        $this->coursecd             = $this->field["COURSECD"];
        $this->majorcd              = $this->field["MAJORCD"];
        $this->coursecode           = $this->field["COURSECODE"];
        $this->perfect              = $this->field["PERFECT"];
        $this->pass_score           = $this->field["PASS_SCORE"];
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz237aQuery::getInsertUpdateQuery($this);
        $this->semester             = $this->field["SEMESTER"];
        $this->testkindcd           = $this->field["TESTKINDCD"];
        $this->subclasscd           = $this->field["SUBCLASSCD"];
        $this->div                  = $this->field["DIV"];
        $this->grade                = $this->field["GRADE"];
        $this->coursecd             = $this->field["COURSECD"];
        $this->majorcd              = $this->field["MAJORCD"];
        $this->coursecode           = $this->field["COURSECODE"];
        $this->perfect              = $this->field["PERFECT"];
        $this->pass_score           = $this->field["PASS_SCORE"];
    }

    //デリート時の内部メソッド
    function &delete() {
        knjz237aQuery::getDeleteQuery($this);
        $this->clean();
    }

    //変数をクリアします
    function clean() {
        unset($this->subclasscd);
        unset($this->div);
        unset($this->grade);
        unset($this->coursecd);
        unset($this->majorcd);
        unset($this->coursecode);
        unset($this->perfect);
        unset($this->pass_score);
    }
}
?>
