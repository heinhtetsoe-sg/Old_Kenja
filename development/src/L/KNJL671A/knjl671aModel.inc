<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl671aModel extends Model {
    var $cmd;
    var $field      = array();

    var $objYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl671aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                  //職員コード
        $this->objYear = CTRL_YEAR + 1;
    }

    function init() {
        $this->cmd         = VARS::request("cmd");

        unset($this->sortColumn);
        unset($this->sortOrder);
        if (VARS::post("SORT_COLUMN")) {
            $this->sortColumn = VARS::post("SORT_COLUMN");
        }
        if (VARS::post("SORT_ORDER")) {
            $this->sortOrder = VARS::post("SORT_ORDER");
        }

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV")    //入試制度(校種)
                           , "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
                           , "EXAMCOURSECD" =>  VARS::post("EXAMCOURSECD")    //合格コース
                        );

        if ($this->field["TESTDIV"] == "1") {
            $this->scholarCdColumnName = "REMARK7";
        } else {
            $this->scholarCdColumnName = "REMARK8";
        }

        if ($this->cmd == 'update') {
            $this->line = array(
                  "EXAMNO"        =>  VARS::post("EXAMNO")          // 受験番号
                , "SP_SCHOLAR_CD" =>  VARS::post("SP_SCHOLAR_CD")   // 特別奨学生コード
            );
        }
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();


        // 特別奨学生コードのチェック
        for ($i=0; $i < get_count($this->line["EXAMNO"]); $i++) { 
            $receptNo   = $this->line["EXAMNO"][$i];
            $scholarCd = $this->line["SP_SCHOLAR_CD"][$i];
        }

        Query::dbCheckIn($db);
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            $result = knjl671aQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }

}
?>
