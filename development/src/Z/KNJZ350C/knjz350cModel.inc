<?php

require_once('for_php7.php');

class knjz350cModel extends Model
{
    public $field = array();
    public $cmd;
    public $selectdata1;
    public $selectdata2;
    public $selectdata3;

    //メンバ変数の初期化
    public function knjz350cModel()
    {
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->school_kind);
        }

        $this->field["PRG_ID"]          = VARS::post("PRG_ID");
        $this->field["GROUPCD"]         = VARS::post("GROUPCD");
        $this->field["GRADE"]           = "00";
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"]    = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"]    = "0-000";
        }
        $this->selectdata1              = VARS::post("selectdata1");
        $this->selectdata2              = VARS::post("selectdata2");
        $this->selectdata3              = VARS::post("selectdata3");
        $this->itemList                 = VARS::post("itemList");
        $this->cmd                      = VARS::request("cmd");

        //プログラムID => ATTEND_DIV(1:ATTEND_SEMES_DAT 2:ATTEND_SUBCLASS_DAT)
        $this->prgid = array();
        $this->prgid["KNJC031F"] = "1";
        $this->prgid["KNJC032F"] = "1";
        $this->prgid["KNJC035F"] = "2";

        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem[]["LESSON"]       = array("0000", "",     array("1", "授業日数"), array("1", "授業時数"));
        $this->attendItem[]["OFFDAYS"]      = array("A004", "2",    array("1", ""), array("1", ""));
        $this->attendItem[]["ABROAD"]       = array("A004", "1",    array("1", ""), array("1", ""));
        $this->attendItem[]["ABSENT"]       = array("C001", "1",    array("1", ""), array("1", ""));
        $this->attendItem[]["SUSPEND"]      = array("C001", "2",    array("1", ""), array("1", ""));
        if ($this->Properties["useKoudome"] == "true") {
            $this->attendItem[]["KOUDOME"]      = array("C001", "25",   array("1", ""), array("1", ""));
        }
        if ($this->Properties["useVirus"] == "true") {
            $this->attendItem[]["VIRUS"]        = array("C001", "19",   array("1", ""), array("1", ""));
        }
        $this->attendItem[]["MOURNING"]     = array("C001", "3",    array("1", ""), array("1", ""));
        $this->attendItem[]["ATTEND"]       = array("0000", "",     array("1", "出席すべき日数"), array("", ""));
        $this->attendItem[]["SICK"]         = array("C001", "4",    array("1", ""), array("1", ""));
        $this->attendItem[]["NOTICE"]       = array("C001", "5",    array("1", ""), array("1", ""));
        $this->attendItem[]["NONOTICE"]     = array("C001", "6",    array("1", ""), array("1", ""));
        $this->attendItem[]["KESSEKI"]      = array("0000", "",     array("1", "欠席日数"), array("", ""));
        $this->attendItem[]["PRESENT"]      = array("0000", "",     array("1", "出席日数"), array("", ""));
        $this->attendItem[]["NURSEOFF"]     = array("C001", "14",   array("",  ""), array("1", ""));
        $this->attendItem[]["LATE"]         = array("C001", "15",   array("1", ""), array("1", ""));
        $this->attendItem[]["EARLY"]        = array("C001", "16",   array("1", ""), array("1", ""));
        $this->attendItem[]["DETAIL_001"]   = array("C002", "001",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_002"]   = array("C002", "002",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_003"]   = array("C002", "003",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_004"]   = array("C002", "004",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_101"]   = array("C002", "101",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_102"]   = array("C002", "102",  array("1", ""), array("",  ""));

        //学校種別
        if (VARS::post("SCHOOL_KIND")) {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!strlen($this->field["PRG_ID"])) {
            $this->warning = "対象プログラムを選択してください。";
            return false;
        }
        if (!strlen($this->field["GROUPCD"])) {
            $this->warning = "対象グループを選択してください。";
            return false;
        }
        if ($this->Properties["use_school_detail_gcm_dat"] == '1' && !strlen($this->field["COURSE_MAJOR"])) {
            $this->warning = "対象課程学科を選択してください。";
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $selectdata = $flg = "";
        if ($this->cmd == "update1") {
            $flg = "1";
            $selectdata = $this->selectdata1;
        } elseif ($this->cmd == "update2") {
            $flg = "2";
            $selectdata = $this->selectdata2;
        } elseif ($this->cmd == "update3") {
            $flg = "3";
            $selectdata = $this->selectdata3;
        }

        //更新処理
        knjz350cQuery::getUpdateQuery($this, $selectdata, $flg);
    }

    //前年度からのコピーのモデルを返します
    public function &getCopyModel()
    {
        knjz350cQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
    }
}
