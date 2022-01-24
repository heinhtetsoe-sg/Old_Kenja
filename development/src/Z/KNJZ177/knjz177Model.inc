<?php

require_once('for_php7.php');
class knjz177Model extends Model {

    var $cmd          = "";
    var $TARGET_MONTH = "";
    var $YEAR         = "";
    var $SEMESTER     = "";
    var $MONTH        = "";
    var $SIMEBI       = "";
    var $school_kind  = "";

    function knjz177Model() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        if (VARS::post("year")!=""){
            $this->year = VARS::post("year");
        } else {
            $this->year = CTRL_YEAR;
        }

        $this->TARGET_MONTH = VARS::post("TARGET_MONTH");
        $this->SIMEBI       = sprintf("%02s", VARS::post("SIMEBI"));
        $target_month_array = explode(",", $this->TARGET_MONTH); //年度、月、学期、締め日(チェック用)がカンマ区切りだからそれを配列にする
        $this->YEAR     = $target_month_array[0];
        $this->MONTH    = $target_month_array[1];
        $this->SEMESTER = $target_month_array[2];
        $this->school_kind  = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->school_kind;

        //セキュリティーチェック
        //STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD,"knjz177");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        //締め日文字数チェック
        if (strlen($this->SIMEBI) > 2) {
            $this->setWarning("MSG915","締め日 2バイトまでです");
            return false;
        }
        //数値であるかチェック
        if ($this->SIMEBI != strval(intval($this->SIMEBI)) && $this->SIMEBI != '') {
            $this->setWarning("MSG901", "半角数字を入力してください。");
            return false;
        }
        //そのつきの最終日より大きいかチェック
        $setYear = $this->MONTH < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
        $lastday = date("t", mktime( 0, 0, 0, $this->MONTH, 1, $setYear));
        if ($this->SIMEBI > $lastday) {
            $this->setWarning("MSG901", "{$this->MONTH}月の最終日は{$lastday}です。");
            return false;
        }
        //学期の境目をまたいでないかチェック
        $query = knjz177Query::checker($this->YEAR, $this->SEMESTER);
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        if ($this->MONTH == $row['SMONTH'] && $this->SIMEBI < $row['SDAY']) {
            $this->setWarning("MSG901", "日付が学期をまたいでいます。開始日は{$row['SDAY']}日です");
            return false;
        } else if ($this->MONTH == $row['EMONTH'] && $this->SIMEBI > $row['EDAY']) {
            $this->setWarning("MSG901", "日付が学期をまたいでいます。終了日は{$row['EDAY']}日です");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            $db->autoCommit(false);
            $query = knjz177Query::getDeleteQuery($this);
            $db->query($query);
            if ($this->SIMEBI != '') {
                $query = knjz177Query::getUpdateQuery($this, "ATTEND_SEMES_DAT");
                $db->query($query);
                $query = knjz177Query::getUpdateQuery($this, "ATTEND_SUBCLASS_DAT");
                $db->query($query);
                $query = knjz177Query::getInsertQuery($this);
                $db->query($query);
            }
            $db->commit(); // トランザクションをコミットする。
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
    }
}
?>
