<?php

require_once('for_php7.php');

class knjz023Model extends Model {

    var $cmd            = "";
    var $field          = array();
    var $fields         = array();
    var $data           = array();
    var $TARGET_MONTH   = "";
    var $SEMESTER       = "";
    var $MONTH          = "";
    var $set            = "";

    function knjz023Model() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            unset($this->fields);
        }

        $this->field = array("GRADE"        =>  VARS::request("GRADE"),     //学年
                             "COURSE"       =>  VARS::post("COURSE"),       //課程学科
                             "COPY_COURSE"  =>  VARS::post("COPY_COURSE"),  //課程学科（コピー元）
                             "HR_CLASS_DIV" =>  VARS::post("HR_CLASS_DIV")  //クラス区分
                             );

        if (VARS::post("sem_month_list")) {
            $sem_month_array = explode(',', VARS::post("sem_month_list"));
            foreach($sem_month_array as $key) {
                $this->fields["LESSON"][$key] = VARS::post("LESSON".$key);
                $this->fields["AUTO_CHECK"][$key] = VARS::post("AUTO_CHECK".$key);
            }
        }

        //セキュリティーチェック
        //STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD,"knjz023");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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
            $query = knjz023Query::getDeleteQuery($this);
            $db->query($query);

            //更新
            foreach($this->fields["LESSON"] as $key => $val) {
                list($semester, $month) = preg_split("/-/", $key);
                //授業日数がnullは作成しない
                if ($val != "") {
                    $query = knjz023Query::getInsertQuery($this, $semester, $month, $val);
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
