<?php
class knje063dModel extends Model
{
    public $cmd;
    public $schregno;      //学籍番号
    public $field;         //画面上のデータ
    public $grade;         //学年
    public $annual;        //学年(年次)
    public $control;
    public $mode;          //在籍卒業モード
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $schArray = array();
    public $readYear;
    public $itemname;

    public function knje063dModel()
    {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->classcds = array("30-P", "31-P", "32-P", "35-P", "36-P", "37-P", "91-P", "92-P");
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
            $this->year = CTRL_YEAR;
        }

        $this->cmd     = VARS::request("cmd");
        //次の生徒
        $this->nextURL = VARS::post("nextURL");

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::request("SCHREGNO")) {
            $this->schregno   = VARS::request("SCHREGNO");
        }
        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }

        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->mode = VARS::get("mode");         //在籍卒業モード
        }

        $this->field = array();
        foreach ($this->classcds as $classcd) {
            $subclasscd = VARS::post("SUBCLASS_".$classcd);
            $this->field["SUBCLASS_".$classcd] = $subclasscd;
        }
        if (!in_array($this->cmd, array("add_year", "updEdit"))) {
            foreach ($this->classcds as $classcd) {
                $subclasscd = $this->field["SUBCLASS_".$classcd];
                if ($subclasscd != "") {
                    $this->field["REMARK_".$subclasscd] = VARS::post("REMARK_".$classcd);
                }
            }
        }
        $this->field["REMARK_"] = "";

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->getFieldSize();
    }

    //入力項目チェック
    public function validateInput($db)
    {
        if (!isset($this->schregno)) {
            return false;
        }
        if ($this->cmd == "update") {
            foreach ($this->classcds as $classcd) {
                $subclasscd = $this->field["SUBCLASS_{$classcd}"];
                if ($subclasscd == '') {
                    $row = $db->getRow(knje063dQuery::getClassmst($classcd), DB_FETCHMODE_ASSOC);
                    $this->setWarning("MSG901", $row["LABEL"]."の科目コードを指定してください");
                    return false;
                }
            }
        }

        foreach ($this->classcds as $classcd) {
            $subclasscd = $this->field["SUBCLASS_{$classcd}"];
            $remark = $this->field["REMARK_{$subclasscd}"];

            if ($this->validate_row_cnt($remark, ($this->moji * 2)) > $this->gyou) {
                $row = $db->getRow(knje063dQuery::getClassmst($classcd), DB_FETCHMODE_ASSOC);
                $name = "文言評価（".$row["LABEL"]."）";
                $this->setWarning("MSG915", "({$name}は{$this->gyou}行までです)");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        if ($this->validateInput($db)) {
            if ($this->cmd == "update") {
                //更新処理
                $db->autoCommit(false);
                knje063dQuery::update($db, $this);
                $db->commit(); // トランザクションをコミットする。
                $this->setMessage("MSG201");
            } elseif ($this->cmd == "delete") {
                //更新処理
                $db->autoCommit(false);
                knje063dQuery::deleteYear($db, $this);
                $db->commit(); // トランザクションをコミットする。
                $this->setMessage("データを削除しました");
                $this->field = array();
                $this->year = "";
            }
        }
        Query::dbCheckIn($db);
    }

    public function getFieldSize()
    {
        $this->moji = 9;
        $this->gyou = 8;
    }
}
?>
