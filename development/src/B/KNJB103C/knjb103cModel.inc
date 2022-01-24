<?php

require_once('for_php7.php');

class knjb103cModel extends Model
{
    public $control;
    public $cmd;
    public $fields = array();
    public $grade;
    public $semester;
    public $testcd;
    public $classcd;
    public $subclasscd;
    public $data_cnt;
    public $counter;
    public $staffChaircd;
    public $staffStaffcd;
    public $replace_data = array();    //一括更新で選択された講座一覧
    public $lastColumn;//CSVの最後の列の値

    public function knjb103cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::request("PROGRAMID") != "") {
            unset($this->grade);
            unset($this->semester);
            unset($this->testcd);
            unset($this->classcd);
            unset($this->subclasscd);
        }

        $this->grade      = VARS::request("GRADE");
        $this->semester   = VARS::request("SEMESTER");
        $this->testcd     = VARS::request("TESTCD");
        $this->classcd    = VARS::request("CLASSCD");
        $this->subclasscd = VARS::request("SUBCLASSCD");

        $this->data_cnt = VARS::post("DATA_CNT");

        $this->cmd   = VARS::request("cmd");
        $this->counter = VARS::get("Counter");
        $this->staffChaircd = VARS::get("STAFF_CHAIRCD");
        $this->staffStaffcd = VARS::get("STAFF_STAFFCD");

        //初期化
        $this->fields = array();
        if ($this->cmd == 'update') {
            for ($counter = 0; $counter < $this->data_cnt; $counter++) {
                $this->fields[$counter]["CHAIRCD"]         = VARS::post("CHAIRCD"."-".$counter);
                $this->fields[$counter]["PROCTOR_STAFFCD"] = VARS::post("PROCTOR_STAFFCD"."-".$counter);
                $this->fields[$counter]["RETURN_STAFFCD"]  = VARS::post("RETURN_STAFFCD"."-".$counter);
                $this->fields[$counter]["Q_PAPERS"]        = VARS::post("Q_PAPERS"."-".$counter);
                $this->fields[$counter]["A_PAPERS"]        = VARS::post("A_PAPERS"."-".$counter);
                $this->fields[$counter]["Q_BOTH_DIV"]      = VARS::post("Q_BOTH_DIV"."-".$counter);
                $this->fields[$counter]["A_BOTH_DIV"]      = VARS::post("A_BOTH_DIV"."-".$counter);
                $this->fields[$counter]["DUE_DATE"]        = VARS::post("DUE_DATE"."-".$counter);
                $this->fields[$counter]["DUE_TIME"]        = VARS::post("DUE_TIME"."-".$counter);
                $this->fields[$counter]["REMARK"]          = VARS::post("REMARK"."-".$counter);
            }
        }

        //一括更新
        $this->replace_data = array();
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["field"] = array();

            //チェックされた項目をテーブル別に分ける
            $this->replace_data["check"] = array(VARS::post("RCHECK0"),
                                                 VARS::post("RCHECK1"),
                                                 VARS::post("RCHECK2"),
                                                 VARS::post("RCHECK3"),
                                                 VARS::post("RCHECK4"),
                                                 VARS::post("RCHECK5"),
                                                 VARS::post("RCHECK6"),
                                                 VARS::post("RCHECK7"),
                                                );

            $this->replace_data["check_all"] = VARS::post("RCHECKALL");

            if (VARS::post("RCHECK0") =="1") {
                $this->replace_data["field"]["RETURN_STAFFCD"] = VARS::post("RETURN_STAFFCD");
            }
            if (VARS::post("RCHECK1") =="1") {
                $this->replace_data["field"]["Q_PAPERS"] = VARS::post("Q_PAPERS");
            }
            if (VARS::post("RCHECK2") =="1") {
                $this->replace_data["field"]["A_PAPERS"] = VARS::post("A_PAPERS");
            }
            if (VARS::post("RCHECK3") =="1") {
                $this->replace_data["field"]["Q_BOTH_DIV"] = VARS::post("Q_BOTH_DIV");
            }
            if (VARS::post("RCHECK4") =="1") {
                $this->replace_data["field"]["A_BOTH_DIV"] = VARS::post("A_BOTH_DIV");
            }
            if (VARS::post("RCHECK5") =="1") {
                $this->replace_data["field"]["DUE_DATE"] = VARS::post("DUE_DATE");
            }
            if (VARS::post("RCHECK6") =="1") {
                $this->replace_data["field"]["DUE_TIME"] = VARS::post("DUE_TIME");
            }
            if (VARS::post("RCHECK7") =="1") {
                $this->replace_data["field"]["REMARK"] = VARS::post("REMARK");
            }
        }

        if (VARS::get("ini2")) {
            unset($this->replace_data);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        foreach ($this->fields as $counter => $value) {
            if (!strlen($value["CHAIRCD"])) {
                $this->setWarning("MSG301", "(講座コード)");
                return false;
            }

            //文字数チェック
            if (strlen($value["Q_PAPERS"]) > 3) {
                $this->setWarning("MSG915", "講座コード ".$value["CHAIRCD"]." (問題用紙枚数) 3バイトまでです。");
                return false;
            } elseif (strlen($value["A_PAPERS"]) > 3) {
                $this->setWarning("MSG915", "講座コード ".$value["CHAIRCD"]." (解答用紙枚数) 3バイトまでです。");
                return false;
            } elseif ($this->validate_row_cnt($value["REMARK"], (20 * 2)) > 3) {
                $this->setWarning("MSG915", "講座コード ".$value["CHAIRCD"]." (備考は3行までです)");
                return false;
            }
        }
        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjb103cQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //一括更新
    public function &replaceModel()
    {
        //講座が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setCmd("replace");
            $this->setWarning("MSG304");
            return false;
        }

        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["check"])) {
            $this->setCmd("replace");
            $this->setWarning("更新する項目がありません");
            return false;
        }

        //文字数チェック
        if (strlen($this->replace_data["field"]["Q_PAPERS"]) > 3) {
            $this->setWarning("MSG915", "(問題用紙枚数) 3バイトまでです。");
            return false;
        } elseif (strlen($this->replace_data["field"]["A_PAPERS"]) > 3) {
            $this->setWarning("MSG915", "(解答用紙枚数) 3バイトまでです。");
            return false;
        } elseif ($this->validate_row_cnt($this->replace_data["field"]["REMARK"], (20 * 2)) > 3) {
            $this->setWarning("MSG915", "(備考は3行までです)");
            return false;
        }

        $db = Query::dbCheckOut();

        //チェックがついている項目があるテーブルのみを更新
        $result = knjb103cQuery::replaceUpdateQuery($this);

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
