<?php

require_once('for_php7.php');
class knjf333Model extends Model {
    var $field        = array();
    var $cmd;
    var $schregno;
    var $regddate;      //登録日付
    var $subclasscd;    //科目コード
    var $condition;     //学修・併修
    var $seq;           //回数
    var $name;          //生徒氏名
//※教育課程対応
    var $classcd;

    function knjf333Model() {}

    function init() {
        //クッキー(?)の値をクリアする
        if (!VARS::get("EXP_YEAR") && !VARS::get("REGDDATE") && $this->cmd == "edit") {
            unset($this->regddate);
        }

        if(VARS::get("PROGRAMID")){
            unset($this->schregno);
            unset($this->name);
        }

        $this->dataFieldType = array("001_01" => "C",
                                     "001_02" => "C",
                                     "002_01" => "I",
                                     "002_02" => "I",
                                     "003_01" => "I",
                                     "004_01" => "I",
                                     "005_01" => "I",
                                     "006_01" => "I",
                                     "006_02" => "I");

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf333Query::getSchoolMst();

        $this->field["SCH_NASI"] = VARS::post("SCH_NASI");
        $this->field["AGE"] = VARS::post("AGE");
        $this->field["SEX"] = VARS::post("SEX");
        foreach ($this->dataFieldType as $divSeq => $divType) {
            $this->field[$divSeq] = VARS::post("DATA{$divSeq}");
        }

        $this->execute_date = VARS::post("EXECUTE_DATE");

        $this->cmd = VARS::request("cmd");

        if(VARS::get("SCHREGNO")    !="") $this->schregno    = VARS::get("SCHREGNO");

        //校種取得
        $rtnRow = knjf333Query::getSchoolKind($this);
        $this->schoolKind = $rtnRow["SCHOOL_KIND"];

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput($kind) {
        if ($this->schregno == "" && $this->field["SCH_NASI"] == "" && $this->gaitouNasi == 0) {
           $this->setWarning("MSG304","(生徒を選択/該当児童なしを選択して下さい。)");
           return false;
        }

        if ($kind=="update" || $kind=="insert") {
            if (strlen($this->field["001_01"]) > 150) {
                $this->setWarning("MSG915","(病気は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["001_02"]) > 150) {
                $this->setWarning(MSG915,"(けがは150バイトまでです)");
                return false;
            }
        }
        return true;
    }

    //追加
    function &getInsertModel() {
        if ($this->validateInput("insert")){
            $this->insert();
        }
    }

    //削除
    function &getDeleteModel() {
        if ($this->validateInput("delete")){
            $this->delete();
        }
    }

    function &delete() {
        knjf333Query::getDeleteHdatQuery($this);
        knjf333Query::getDeleteDatQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();
    }

    function &insert() {
        if ($this->schregno) {
            knjf333Query::getDeleteHdatQuery($this);
            knjf333Query::getDeleteDatQuery($this);
        }
        knjf333Query::getInsertQuery($this);
        $this->setMessage("MSG201");
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf333Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "年組番";
        $header[] = "氏名";
        $header[] = "年齢";
        $header[] = "性別";
        $header[] = "病気";
        $header[] = "けが";
        $header[] = "欠席日数";
        $header[] = "継続区分";
        $header[] = "入院";
        $header[] = "保健室登校理由";
        $header[] = "医師の診断";
        $header[] = "保健室登校日数";
        $header[] = "継続区分";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf333Query::selectCsvQuery($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "感染症発生状況入力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
