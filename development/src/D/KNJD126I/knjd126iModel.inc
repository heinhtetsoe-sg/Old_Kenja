<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126iModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $term;
    var $gen_ed = "900100";        //総合学習コード
    var $select;
    var $nyuryoku;
    var $type_div;
    var $nonVisualViewCd;

    /**
     * メンバ変数の初期化
     */
    function knjd126iModel()
    {
        $this->field = array();
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

    }

    function init()
    {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->select);
            unset($this->nyuryoku);
            unset($this->type_div);
            unset($this->nonVisualViewCd);
        }

        $this->cmd     = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["SEMESTER2"] = VARS::post("SEMESTER2");

        //講座コード
        if($this->field["CLASSCD"] == VARS::post("CLASSCD") && VARS::post("CHAIRCD")){
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD_SUBCLASS"] = "";
        }

        //科目コード
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        if ($this->cmd == "update" || $this->cmd == "updateRecordSeme") {
            if ($this->Properties["use_prg_schoolkind"] == "1") $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("H_CHAIRCD");
        }

        //入力方法選択
        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }

        //入力選択
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }

        //入力値選択
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        /*  更新処理 */

        //初期化
        $this->fields = array();
        for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {

            foreach ($this->data["STATUS"] as $code => $col) {

                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {

                    $this->fields["STATUS".$code][$i] = $post_term;

                }
            }
        }

        knjd126iQuery::update($this);
        knjd126iQuery::updateRecord($this);
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "form1" : "form2";
    }

    function &getUpdateRecordSeme()
    {
        $db = Query::dbCheckOut();

        //初期化
        $this->fields = array();
        for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "STATUS9-".$i;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields[$this->data["SCHREGNO"][$i]] = $post_term;
            }
        }

        knjd126iQuery::setRecordScoreSemeVal($db, $this);
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "form1" : "form2";

        Query::dbCheckIn($db);
    }

    function getTotalData(&$arg, $db, $kantenArray, $execute_date, $view_key, $param)
    {
        $query = knjd126iQuery::getGrade($this, $execute_date);
        $result = $db->query($query);
        $this->gradeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->gradeArray[$row["GRADE"]] = $row["GRADE_NAME1"];
        }
        $result->free();
        $this->gradeArray["CLASS"] = "";
        foreach ($this->gradeArray as $grade => $gName) {
            $setData = array();
            if ($grade == "CLASS") {
                $setNameShow = "参照教科のクラスの観点評価人数　　　　　　";
            } else {
                $setNameShow = "参照教科の{$gName}の観点評価人数　　　　　　";
            }
            $sep = "";
            foreach ($kantenArray as $key => $val) {
                $setMark = $val["VAL"] == "F" ? "" : $val["VAL"];
                $setNameShow .= $sep.$val["VAL"];
                $sep = "<br>";
            }
            $sep = "";
            foreach ($kantenArray as $key => $val) {
                if ($setNameShow) {
                    $setData["NAME_SHOW"] = $setNameShow;
                    $setData["NAME_SHOW_SPAN"] = get_count($kantenArray);
                }
                $setNameShow = "";

                $setMark = $val["VAL"] == "F" ? "" : $val["VAL"];
                $query = knjd126iQuery::selectTotalQuery($param, $execute_date, $view_key, $grade, $setMark);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    foreach ($row as $setCntField => $setCntVal) {
                        $setData[$setCntField] .= $sep.$setCntVal;
                    }
                }
                $result->free();
                $sep = "<br>";

            }
            $arg["dataTotal"][] = $setData;
        }
    }
}
?>
