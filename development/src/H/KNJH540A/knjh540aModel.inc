<?php

require_once('for_php7.php');

class knjh540aModel extends Model {
    var $field        = array();
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持

    function knjh540aModel() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD, "KNJH540A");
        if (VARS::request("cmd") == '') {
            unset($this->semester);
            unset($this->proficiencydiv);
            unset($this->proficiencycd);
        }
        if (VARS::post("cmd") == "list") {
            unset($this->proficiencydiv);
            unset($this->proficiencycd);
        }
        $this->cmd  = VARS::request("cmd");
        $this->field = array("PROFICIENCYDIV"           => VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD"            => VARS::post("PROFICIENCYCD"),
                             "PROFICIENCYCD_FROMCOPY"   => VARS::post("PROFICIENCYCD_FROMCOPY"),
                             "LEFT_GRADE"               => VARS::post("LEFT_GRADE"),
                             "PROFICIENCY_SUBCLASS_CD"  => VARS::post("PROFICIENCY_SUBCLASS_CD"),
                             "DIV"                      => sprintf('%02d', VARS::post("DIV")),
                             "GRADE"                    => VARS::post("GRADE"),
                             "COURSECD"                 => VARS::post("COURSECD"),
                             "MAJORCD"                  => VARS::post("MAJORCD"),
                             "COURSECODE"               => VARS::post("COURSECODE"),
                             "PERFECT"                  => VARS::post("PERFECT"),
                             "PASS_SCORE"               => VARS::post("PASS_SCORE"),
                             "WEIGHTING"                => VARS::post("WEIGHTING")
                             );

        if (VARS::get("cmd") == "edit"){
            $this->proficiencydiv           = VARS::get("PROFICIENCYDIV");
            $this->proficiencycd            = VARS::get("PROFICIENCYCD");
            $this->proficiency_subclass_cd  = VARS::get("PROFICIENCY_SUBCLASS_CD");
            $this->div                      = VARS::get("DIV");
            $this->grade                    = VARS::get("GRADE");
            $this->coursecd                 = VARS::get("COURSECD");
            $this->majorcd                  = VARS::get("MAJORCD");
            $this->coursecode               = VARS::get("COURSECODE");
            $this->perfect                  = VARS::get("PERFECT");
            $this->pass_score               = VARS::get("PASS_SCORE");
            $this->weighting                = VARS::get("WEIGHTING");
        }

        if (VARS::get("cmd") == "edit" || $this->cmd == "reset"){
            $this->field["PROFICIENCYDIV"]          = $this->proficiencydiv;
            $this->field["PROFICIENCYCD"]           = $this->proficiencycd;
            $this->field["DIV"]                     = $this->div;
            $this->field["PROFICIENCY_SUBCLASS_CD"] = $this->proficiency_subclass_cd;
            $this->field["GRADE"]                   = $this->grade;
            $this->field["COURSECD"]                = $this->coursecd;
            $this->field["MAJORCD"]                 = $this->majorcd;
            $this->field["COURSECODE"]              = $this->coursecode;
            $this->field["PERFECT"]                 = $this->perfect;
            $this->field["PASS_SCORE"]              = $this->pass_score;
            $this->field["WEIGHTING"]               = $this->weighting;
        }

        if (VARS::get("cmd") == "list_update") {
            $this->field["PROFICIENCYDIV"] = $this->proficiencydiv;
            $this->field["PROFICIENCYCD"] = $this->proficiencycd;
        }

        if (VARS::request("PROFICIENCYDIV")) {
            $this->proficiencydiv = VARS::request("PROFICIENCYDIV");
            $this->proficiencycd = VARS::request("PROFICIENCYCD");
        }

        $this->semester = VARS::request("SEMESTER");

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //入力チェック
        if (trim($this->semester) == "") {
            $this->setWarning('MSG301', "(学期)");
            return false;
        }
        if(trim($this->field["PROFICIENCYDIV"]) == "") {
            $this->setWarning('MSG301', "(実力区分)");
            return false;
        }
        if(trim($this->field["PROFICIENCYCD"]) == "") {
            $this->setWarning('MSG301', "(実力テスト)");
            return false;
        }
        if(trim($this->field["PROFICIENCY_SUBCLASS_CD"]) == "") {
            $this->setWarning('MSG301', "(科目)");
            return false;
        }
        if(trim($this->field["DIV"]) == "") {
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
        if(strlen($this->field["PERFECT"]) > 3 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(文字数が不正です。)");
            return false;
        }
        if($this->field["WEIGHTING"] >= 10 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(傾斜の整数値は１桁にしてください)");
            return false;
        }
        //update,delete共通のチェック
        if($this->cmd == "update" || $this->cmd == "delete"){
            if($this->proficiencydiv != $this->field["PROFICIENCYDIV"] || $this->proficiencycd != $this->field["PROFICIENCYCD"]){
                $this->setWarning('MSG308',"(実力名)");
                return false;
            }elseif($this->proficiency_subclass_cd != $this->field["PROFICIENCY_SUBCLASS_CD"]){
                $this->setWarning('MSG308',"(科目)");
                return false;
            }elseif($this->div != $this->field["DIV"]){
                $this->setWarning('MSG308',"(区分)");
                return false;
            }elseif($this->div !== $this->field["DIV"]){
            echo $this->div ."!==". $this->field["DIV"]."<hr>";
                if($this->cmd == "update"){
                    $this->setWarning('データが不正です。','更新に失敗しました。');
                    return false;
                } else {
                    $this->setWarning('MSG919');
                    return false;
                }
            }elseif($this->grade != $this->field["GRADE"]){
                $this->setWarning('MSG308',"(学年)");
                return false;
            }elseif($this->coursecd != $this->field["COURSECD"]){
                $this->setWarning('MSG308',"(課程)");
                return false;
            }elseif($this->majorcd != $this->field["MAJORCD"]){
                $this->setWarning('MSG308',"(学科)");
                return false;
            }elseif($this->coursecode != $this->field["COURSECODE"]){
                $this->setWarning('MSG308',"(コース)");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            $db = Query::dbCheckOut();
            $query = knjh540aQuery::getExists($this);
            $ret_val = $db->getOne($query);
            if (!$ret_val) {
                $this->setWarning('MSG303');
                return false;
            }
            Query::dbCheckIn($db);
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel() {
        $cnt = knjh540aQuery::getCopyCountQuery($this);
        if ($cnt == '0') {
            $this->setMessage('\n更新対象がありませんでした。');
        } else {
            $result = knjh540aQuery::getCopyQuery($this);
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
    }

    /**
     * コピー元及びコピー先のコピーのモデルを返します
    */
    function &getFromCopyModel() {
        if ($this->field["PROFICIENCYCD"] == "" || $this->field["PROFICIENCYCD_FROMCOPY"] == "") {
            $this->setWarning('MSG304');
            return false;
        }
        $checkcnt = knjh540aQuery::getFromCopyCountQuery($this, "to");
        if ($checkcnt == 0) {
            $cnt = knjh540aQuery::getFromCopyCountQuery($this, "from");
            if ($cnt == '0') {
                $this->setMessage('\n更新対象がありませんでした。');
            } else {
                $result = knjh540aQuery::fromCopyInsertCopy($this);
                $this->setMessage('MSG201');
            }
        } else {
            $this->setWarning('MSG203',"(データが既に登録されています。)");
            return false;
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
        knjh540aQuery::getInsertUpdateQuery($this);
        $this->proficiencydiv           = $this->field["PROFICIENCYDIV"];
        $this->proficiencycd            = $this->field["PROFICIENCYCD"];
        $this->proficiency_subclass_cd  = $this->field["PROFICIENCY_SUBCLASS_CD"];
        $this->div                      = $this->field["DIV"];
        $this->grade                    = $this->field["GRADE"];
        $this->coursecd                 = $this->field["COURSECD"];
        $this->majorcd                  = $this->field["MAJORCD"];
        $this->coursecode               = $this->field["COURSECODE"];
        $this->perfect                  = $this->field["PERFECT"];
    }

    //アップデート時の内部メソッド
    function &update() {
        knjh540aQuery::getInsertUpdateQuery($this);
        $this->proficiencydiv           = $this->field["PROFICIENCYDIV"];
        $this->proficiencycd            = $this->field["PROFICIENCYCD"];
        $this->proficiency_subclass_cd  = $this->field["PROFICIENCY_SUBCLASS_CD"];
        $this->div                      = $this->field["DIV"];
        $this->grade                    = $this->field["GRADE"];
        $this->coursecd                 = $this->field["COURSECD"];
        $this->majorcd                  = $this->field["MAJORCD"];
        $this->coursecode               = $this->field["COURSECODE"];
        $this->perfect                  = $this->field["PERFECT"];
    }

    //デリート時の内部メソッド
    function &delete() {
        knjh540aQuery::getDeleteQuery($this);
        $this->clean();
    }

    //変数をクリアします
    function clean() {
        unset($this->proficiency_subclass_cd);
        unset($this->div);
        unset($this->grade);
        unset($this->coursecd);
        unset($this->majorcd);
        unset($this->coursecode);
        unset($this->perfect);
    }
}
?>
