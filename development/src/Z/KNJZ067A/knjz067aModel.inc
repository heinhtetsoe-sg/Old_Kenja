<?php

require_once('for_php7.php');
class knjz067aModel extends Model
{
    public $field = array();
    public $cmd;
    public $ibyear;
    public $ibgrade;
    public $ibclasscd;
    public $ibprg_course;
    public $ibcurriculum_cd;
    public $ibsubclasscd;
    public $lastColumn;//CSVの最後の列の値

    public function knjz067aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ibyear);
            unset($this->ibgrade);
            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->selectdata);
        }

        list($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) = explode('-', VARS::post("IBSUBCLASS"));

        $this->field = array("IBPRG_COURSE"     =>  VARS::post("IBPRG_COURSE"),
                             "IBCLASSCD"        =>  $ibclasscd,
                             "IBCURRICULUM_CD"  =>  $ibcurriculum_cd,
                             "IBSUBCLASSCD"     =>  $ibsubclasscd,
                             "HEADER"           => VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"           => VARS::post("OUTPUT"),              //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = explode(",", VARS::post("selectdata"));

        //年度（初期値）
        $this->ibyear = ($this->ibyear == "") ? CTRL_YEAR : $this->ibyear;
        //学年（初期値）
        $db = Query::dbCheckOut();
        $this->ibgrade = ($this->ibgrade == "") ? $db->getOne(knjz067aQuery::getIBGrade($this, "list")) : $this->ibgrade;
        Query::dbCheckIn($db);

        if (VARS::post("IBYEAR") != "") {
            $this->ibyear = VARS::post("IBYEAR");
        }
        if (VARS::post("IBGRADE")) {
            $this->ibgrade = VARS::post("IBGRADE");
        }

        //左フレームから取得
        if (VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if (VARS::get("IBPRG_COURSE")) {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if (VARS::get("IBCURRICULUM_CD")) {
            $this->ibcurriculum_cd = VARS::get("IBCURRICULUM_CD");
        }
        if (VARS::get("IBSUBCLASSCD")) {
            $this->ibsubclasscd = VARS::get("IBSUBCLASSCD");
        }
        
        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
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
        //必須チェック
        if (trim($this->ibyear) == "") {
            $this->setWarning("MSG301", "(年度)");
            return false;
        }
        if (trim($this->ibgrade) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->field["IBPRG_COURSE"]) == "") {
            $this->setWarning("MSG301", "(IBコース)");
            return false;
        }
        if (trim($this->field["IBCLASSCD"]) == "" || trim($this->field["IBCURRICULUM_CD"]) == "" || trim($this->field["IBSUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            //更新
            knjz067aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];
            $this->selectdata       = array();
        }
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = $this->ibyear - 1;
        $cnt_pre = $db->getOne(knjz067aQuery::checkIBSubclassReplaceDat($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917", "前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz067aQuery::checkIBSubclassReplaceDat($this->ibyear));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917", "対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz067aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        /*if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $contents = implode(",", $header) . "\r\n";
            $query = knjz067aQuery::getErrorCsvData();
        } else if ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {*/
        //ヘッダ、書出
        $header = array("プログラム",
                            "学年",
                            "IB教科コード",
                            "",
                            "IB教科名",
                            "IB科目コード",
                            "",
                            "",
                            "",
                            "IB科目名",
                            "必要時間",
                            "教科コード",
                            "",
                            "教科名",
                            "科目コード",
                            "",
                            "",
                            "",
                            "科目名",
                            "講座コード",
                            "講座名",
                            "時間単位",
                            $this->lastColumn);
                            
                            
        //if ($this->field["HEADER"]){
        $contents = implode(",", $header) . "\r\n";
        /*} else {
            $contents = "";
        }*/
        $query = knjz067aQuery::getCsvData($this);
        /*}
        if ($this->field["OUTPUT"] == "4") {
            $contents .= "MYP".",";
            $contents .= "02,";
            $contents .= "01,";
            $contents .= "M,";
            $contents .= "Language and literature,";
            $contents .= "01,";
            $contents .= "M,";
            $contents .= "3,";
            $contents .= "010001,";
            $contents .= "Language acquisition,";
            $contents .= "50,";
            $contents .= "01,";
            $contents .= "H,";
            $contents .= "国語,";
            $contents .= "01,";
            $contents .= "H,";
            $contents .= "3,";
            $contents .= "010005,";
            $contents .= "2年国語,";
            $contents .= "0100052,";
            $contents .= "2年講座,";
            $contents .= "140,";
            $contents .= "LASTCOLUMN\n";

            $data = true;
        } else {*/
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($bifKey === $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"]) {
                //重複するIBの科目の必要時間は表示しない
                $bifKey = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];
                //$row["IBCLASSCD"] = "";
                //$row["SUB_IBCLASSCD"] = "";
                //$row["IBPRG_COURSE"] = "";
                //$row["SUB_IBPRG_COURSE"] = "";
                //$row["IBCURRICULUM_CD"] = "";
                //$row["IBSUBCLASSCD"] = "";
                $row["NEED_TIME"] = "";
            //$row["IBCLASSNAME_ENG"] = "";
                    //$row["IBSUBCLASSNAME_ENG"] = "";
            } else {
                //重複するIBの教科、科目は表示しない
                $bifKey = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];
            }
            //配列の値を,で連結
                $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換
                $contents .= implode($row, ",") ."\n";
                
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        //}
        if ($data) {
            /*if ($this->field["OUTPUT"] == "3") {
                $couse="IB教科・科目_学習指導要領教科・科目_関連付けエラー";
            } else {*/
            $couse="IB教科・科目_学習指導要領教科・科目_関連付け一覧";
            //}
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
