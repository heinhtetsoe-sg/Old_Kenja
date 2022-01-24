<?php

require_once('for_php7.php');

class knjf152Model extends Model {

    var $cmd            = "";
    var $field          = array();
    var $fields         = array();
    var $data           = array();
    var $TARGET_MONTH   = "";
    var $MONTH          = "";
    var $set            = "";
    var $year           = "";

    function knjf152Model() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            unset($this->fields);
        }

        if (VARS::request("YEAR")){
            $this->year = VARS::request("YEAR");
        }

        if (VARS::post("month_list")) {
            $month_array = explode(',', VARS::post("month_list"));
            foreach($month_array as $key) {
                $this->fields["LESSON"][$key] = VARS::post("LESSON".$key);
            }
        }

        //セキュリティーチェック
        //STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD,"knjf152");

    }

    //入力項目チェック
    function validateInput($db)
    {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            $db->autoCommit(false);

            //削除
            $query = knjf152Query::getDeleteQuery($this);
            $db->query($query);

            //更新
            foreach($this->fields["LESSON"] as $key => $val) {
                $month = $key;
                //授業日数がnullは作成しない
                if ($val != "") {
                    $query = knjf152Query::getInsertQuery($this, $month, $val);
                    $db->query($query);
                }
            }

            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
    }
}
?>
