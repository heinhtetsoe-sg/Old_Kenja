<?php

require_once('for_php7.php');

class knje063bModel extends Model
{
    public $field = array();
    public $cmd;
    public $schregno;
    public $grade;
    public $hrClass;
    public $subclasscd;
    public $sort;
    public $year = array();
    public $moji;
    public $gyo;
    public $unitcd;

    public function knje063bModel()
    {
        $this->sort["YEAR"] = 1;
        $this->year[0] = CTRL_YEAR;
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            $this->year = array();
        }

        $this->field = array("YEAR"              =>  VARS::post("YEAR"),
                             "CURRICULUM_CD"     =>  VARS::post("CURRICULUM_CD"),
                             "CLASSCD"           =>  VARS::post("CLASSCD"),
                             "SUBCLASSCD"        =>  VARS::post("SUBCLASSCD"),
                             "SUBCLASSCD_SEQ001" =>  VARS::post("SUBCLASSCD_SEQ001"),
                             "REMARK1"           =>  VARS::post("REMARK1")
                             );

        $this->checked = VARS::request("CHECKED");      //チェック

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "edit" && is_string($this->checked)) {
            $a = explode(",", $this->checked);
            $this->editYear    = $a[0];
            $this->subclasscd  = $a[1];
        } elseif ($this->cmd == "right") {
            $this->editYear    = null;
            $this->subclasscd  = null;
        }

        if (VARS::request("SCHREGNO")) {
            $this->schregno   = VARS::request("SCHREGNO");
        }
        if (VARS::request("GRADE")) {
            $this->grade   = VARS::request("GRADE");
        }
        if (VARS::request("HR_CLASS")) {
            $this->hrClass   = VARS::request("HR_CLASS");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
            $this->year = array();  //左のリストから選択されたら初期化
        }

        if (VARS::get("init") == 1) {
            unset($this->schregno);
            unset($this->name);
            $this->year = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year[] = VARS::post("YEAR");
        }

        //指導計画参照画面
        if (VARS::get("TO_DATA")) {
            $this->getData = VARS::get("TO_DATA");
        }
        if (VARS::post("UNITCD") != "") {
            $this->unitcd = VARS::post("UNITCD");
        }

        //DB接続
        $db = Query::dbCheckOut();
        //校種
        //学校校種を取得
        $query = knje063bQuery::getSchoolKind($this);
        $getSchoolKind = $db->getOne($query);
        $query = knje063bQuery::getZ010($this);
        $this->schoolName = $db->getOne($query);
        if ($this->schoolName == "naraken" && $getSchoolKind == "P") {
            $this->moji = 12;
            $this->gyo = 8;
        } else {
            if ($getSchoolKind == "P") {
                $this->moji = 9;
                $this->gyo = ($this->schoolName == "fukuiken") ? 35 : 72;
            } else {
                $this->moji = 24;
                $this->gyo = ($this->schoolName == "fukuiken") ? 35 : 7;
            }            
        }

        //DB切断
        Query::dbCheckIn($db);

        $this->maxRemarkCnt = '8';

        $this->paternInfo = array();
        $this->paternInfo['A'][1] = array("MOJI"        => "35",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][1] = array("MOJI"        => "10",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][2] = array("MOJI"        => "12",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][3] = array("MOJI"        => "18",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][1] = array("MOJI"        => "9",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][2] = array("MOJI"        => "4",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][3] = array("MOJI"        => "11",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][4] = array("MOJI"        => "16",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][1] = array("MOJI"        => "8",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][2] = array("MOJI"        => "8",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][3] = array("MOJI"        => "3",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][4] = array("MOJI"        => "10",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][5] = array("MOJI"        => "16",
                                          "GYOU"        => "25");
    }

    //全体表示用モデルを返します
    public function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("YEAR", "SUBCLASSCD");
        //ソートが押下
        if (trim(VARS::request("sort")) != "") {
            foreach ($sort as $field) {
                if (
                    VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0
                ) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                } elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                } else {
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }

    //入力項目チェック
    public function validateInput(&$db)
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }
        if (!is_numeric($this->field["YEAR"])) {
            $this->setWarning("MSG301", "　　(年度)");
            return false;
        }

        //署名チェック
        if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query = knje063bQuery::getOpinionsWk($this->field["YEAR"], $this->schregno);
            $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $this->setWarning("MSG917", "（署名済み）");
                return false;
            }
        }

        //文言評価
        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->moji * 2)) > $this->gyo) {
            $this->setWarning("MSG915", "(文言評価は{$this->gyo}行までです)");
            // Add by HPA for textarea_cursor start 2020/02/03
                $this->message915="REMARK1";
                // Add by HPA for textarea_cursor end 2020/02/20
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        //入力チェック
        if ($this->validateInput($db)) {
            //削除
            $query = knje063bQuery::getDeleteQuery($this);
            $db->query($query);

            //追加
            $query = knje063bQuery::getInsertQuery($this, $this->field, $db);
            $db->query($query);
            $this->setMessage("MSG201");

            $this->editYear     = $this->field["YEAR"];
            $this->subclasscd   = $this->field["SUBCLASSCD"];
        }
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        $db = Query::dbCheckOut();

        if ($this->cmd == "delete2") {
            //署名チェック
            if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
                $query = knje063bQuery::getOpinionsWk($this->field["YEAR"], $this->schregno);
                $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($check["CHAGE_OPI_SEQ"]) {
                    $this->setWarning("MSG917", "（署名済み）");
                    return false;
                }
            }
            //必須項目が変更されていないかチェック
            if ($this->editYear != $this->field["YEAR"] || $this->subclasscd != $this->field["SUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return;
            }
        }

        //削除
        $query = knje063bQuery::getDeleteQuery($this);
        $db->query($query);

        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
        unset($this->editYear);
        unset($this->subclasscd);
        $this->field = array();
    }
}
?>
