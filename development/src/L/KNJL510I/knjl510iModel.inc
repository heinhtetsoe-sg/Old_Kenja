<?php
//定数定義
class knjl510iModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $examno;

    public $examnoKeta;
    public $nameKeta;
    public $nameKanaKeta;
    public $fsCdKeta;
    public $yearKeta;
    public $zipCdKeta;
    public $addrKeta;
    public $telNoKeta;

    public $maxHopeCourseNum;   //志望コース 最大数
    public $maxAltExamnoNum;    //他受験番号 最大数

    //メンバ変数の初期化
    public function knjl510iModel()
    {
    }

    public function init()
    {
        $this->field = array("TESTDIV0"         =>  VARS::post("TESTDIV0"),         //学科
                            "TESTDIV"           =>  VARS::post("TESTDIV"),          //入試区分
                            "DUPLICATURE"       =>  VARS::post("DUPLICATURE"),      //重複
                            "NAME"              =>  VARS::post("NAME"),             //氏名
                            "NAME_KANA"         =>  VARS::post("NAME_KANA"),        //氏名かな
                            "BIRTHDAY"          =>  VARS::post("BIRTHDAY"),         //生年月日
                            "SEX"               =>  VARS::post("SEX"),              //性別
                            "SEQ031_REMARK5"    =>  VARS::post("SEQ031_REMARK5"),   //過年度
                            "FINSCHOOLCD"       =>  VARS::post("FINSCHOOLCD"),      //出身学校
                            "FS_GRDYEAR"        =>  VARS::post("FS_GRDYEAR"),       //卒業年度
                            "GNAME"             =>  VARS::post("GNAME"),            //保護者氏名
                            "GKANA"             =>  VARS::post("GKANA"),            //保護者氏名かな
                            "GZIPCD"            =>  VARS::post("GZIPCD"),           //郵便番号
                            "GADDRESS1"         =>  VARS::post("GADDRESS1"),        //住所1
                            "GADDRESS2"         =>  VARS::post("GADDRESS2"),        //住所2
                            "GTELNO"            =>  VARS::post("GTELNO"),           //電話番号
                            "HOPE_COURSE1"      =>  VARS::post("HOPE_COURSE1"),     //第一志望
                            "HOPE_COURSE2"      =>  VARS::post("HOPE_COURSE2"),     //第二志望
                            "HOPE_COURSE3"      =>  VARS::post("HOPE_COURSE3"),     //第三志望
                            "HOPE_COURSE4"      =>  VARS::post("HOPE_COURSE4"),     //第四志望
                            "DORMITORY_FLG"     =>  VARS::post("DORMITORY_FLG"),    //奨学希望
                            "SEQ031_REMARK6"    =>  VARS::post("SEQ031_REMARK6"),   //入寮希望
                            "ALT_EXAMNO1"       =>  VARS::post("ALT_EXAMNO1"),      //他受験番号 専願
                            "ALT_EXAMNO2"       =>  VARS::post("ALT_EXAMNO2"),      //他受験番号 前期
                            "ALT_EXAMNO3"       =>  VARS::post("ALT_EXAMNO3")       //他受験番号 後期
                             );
        
        //コース詳細情報読み込み
        $courseDetaiInfo = $this->getCourseDetailInfo();

        //コースに対応する類別をセット
        $this->field["HOPE_CLASSIFICATION1"] = $courseDetaiInfo[$this->field["HOPE_COURSE1"]];     //第一志望 類別
        $this->field["HOPE_CLASSIFICATION2"] = $courseDetaiInfo[$this->field["HOPE_COURSE2"]];     //第二志望 類別
        $this->field["HOPE_CLASSIFICATION3"] = $courseDetaiInfo[$this->field["HOPE_COURSE3"]];     //第三志望 類別
        $this->field["HOPE_CLASSIFICATION4"] = $courseDetaiInfo[$this->field["HOPE_COURSE4"]];     //第四志望 類別
 
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "2";
        $this->examno = VARS::post("EXAMNO");

        //桁数の設定
        $this->examnoKeta = 10;
        $this->nameKeta = 40;
        $this->nameKanaKeta = 80;
        $this->fsCdKeta = 7;
        $this->yearKeta = 4;
        $this->zipCdKeta = 8;
        $this->addrKeta = 100;
        $this->telNoKeta = 14;

        //最大数の設定
        $this->maxHopeCourseNum = 4;
        $this->maxAltExamnoNum = 3;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //コース詳細情報取得
    public function getCourseDetailInfo()
    {
        $db = Query::dbCheckOut();

        $courseDetaiInfo = array();
        $query = knjl510iQuery::getCourseDetailInfoQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseDetaiInfo[$row["GENERAL_CD"]] = $row["HOPE_CLASSIFICATION"];
        }

        Query::dbCheckIn($db);

        return $courseDetaiInfo;
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        $query = knjl510iQuery::getSelectQuery($this, $this->examno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd=="update" || $this->cmd=="back" || $this->cmd=="next") {
            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        if ($this->cmd=="update" || $this->cmd=="add" || $this->cmd=="back" || $this->cmd=="next") {
            //必須チェック
            if (trim($this->field["TESTDIV0"]) == "") {
                $this->setWarning("MSG301", "(学科)");
                return false;
            }
            if (trim($this->field["TESTDIV"]) == "") {
                $this->setWarning("MSG301", "(入試区分)");
                return false;
            }
            if (trim($this->examno) == "") {
                $this->setWarning("MSG301", "(受験番号)");
                return false;
            }
            if (trim($this->field["NAME"]) == "") {
                $this->setWarning("MSG301", "(氏名)");
                return false;
            }
            if (trim($this->field["NAME_KANA"]) == "") {
                $this->setWarning("MSG301", "(氏名かな)");
                return false;
            }
            if (trim($this->field["BIRTHDAY"]) == "") {
                $this->setWarning("MSG301", "(生年月日)");
                return false;
            }
            if (trim($this->field["SEX"]) == "") {
                $this->setWarning("MSG301", "(性別)");
                return false;
            }
            if (trim($this->field["FINSCHOOLCD"]) == "") {
                $this->setWarning("MSG301", "(出身学校)");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["NAME"]) > $this->nameKeta*3) {
                $this->setWarning("MSG915", "(氏名は".($this->nameKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["NAME_KANA"]) > $this->nameKanaKeta*3) {
                $this->setWarning("MSG915", "(氏名かなは".($this->nameKanaKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["GNAME"]) > $this->nameKeta*3) {
                $this->setWarning("MSG915", "(保護者氏名は".($this->nameKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["GKANA"]) > $this->nameKanaKeta*3) {
                $this->setWarning("MSG915", "(保護者氏名かなは".($this->nameKanaKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["GADDRESS1"]) > $this->addrKeta*3) {
                $this->setWarning("MSG915", "(住所1は".($this->addrKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["GADDRESS2"]) > $this->addrKeta*3) {
                $this->setWarning("MSG915", "(住所2は".($this->addrKeta*3)."バイトまでです。)");
                return false;
            }

            //存在チェック 他受験番号
            for ($idx = 1; $idx <= $this->maxAltExamnoNum; $idx++) {
                $value = $this->field["ALT_EXAMNO".$idx];
                if (trim($value) != "") {
                    $db = Query::dbCheckOut();
                    $query = knjl510iQuery::getExamnoExist($this, $value);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    Query::dbCheckIn($db);
                    if (!is_array($row)) {
                        $msg = "専願";
                        if ($idx == 2) {
                            $msg = "前期";
                        } elseif ($idx == 3) {
                            $msg = "後期";
                        }
                        $this->setWarning($msg." 存在しない受験番号。".$idx);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl510iQuery::getUpdateQuery($this);
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl510iQuery::getUpdateQuery($this);
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
        }
    }
    
    //斜線あり日付に変換
    public function getSlashDate($preFormattedDate)
    {
        $y = substr($preFormattedDate, 0, 4);
        $m = substr($preFormattedDate, 4, 2);
        $d = substr($preFormattedDate, 6, 2);
    
        $date = $y;
        if ($m) {
            $date.="/".$m;
        }
        if ($d) {
            $date.="/".$d;
        }
        return $date;
    }
}
