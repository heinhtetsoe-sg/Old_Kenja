<?php

require_once('for_php7.php');

//メニュー画面
class knjl691iModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv0;
    public $testdiv;
    public $field;
    public $match;    //マッチ件数
    public $unmatch1; //アンマッチ件数（願書）
    public $unmatch2; //アンマッチ件数（募集）

    public function init()
    {
        $this->examyear         = CTRL_YEAR + 1;
        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = "2"; //高校固定
        $this->testdiv0         = VARS::request("TESTDIV0");
        $this->testdiv          = VARS::request("TESTDIV");
        $this->field = array();

        if ($this->cmd != "match") {
            $this->match    = "";
            $this->unmatch1 = "";
            $this->unmatch2 = "";
        }
    }

    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
    }
    // 実行のモデルを返します
    public function &getExecModel()
    {
        knjl691iQuery::exec($this);

        if ($this->match > 0) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG202");
        }
    }

    // CSV出力
    public function &getCsvModel()
    {
        $db = Query::dbCheckOut();
        $testDivName = $db->getOne(knjl691iQuery::getTestdivMst($this->examyear, $this->applicantdiv, $this->testdiv, "true"));

        $contents = ""; //クリア
        
        //タイトル
        $contents .= ",,,$testDivName,募集・願書チェックリスト,,,,,".date("Y/m/d H:i");
        $contents .= "\n";

        //ヘッダー
        $header = array();
        $header["EXAMNO"]        = "受験番号";
        $header["DIV"]           = "";
        $header["NAME"]          = "氏名";
        $header["KANA"]          = "カナ氏名";
        $header["SEX"]           = "性別";
        $header["SOUDAN"]        = "相談ｺｰｽ";
        $header["FS_CD"]         = "中学校ｺｰﾄﾞ";
        $header["SCHOOL_NAME"]   = "中学校名";
        $header["COMMON_TEST"]   = "共通テスト";
        $header["KESSEKI"]       = "欠席日数";
        $header["KESSEKI_RIYUU"] = "欠席理由書";
        $header["TOKUTAI"]       = "特待記号";
        $header["BUKATSUNO"]     = "部活番号";
        $header["BIKOU"]         = "備考";
        
        $data = false; //出力フラグ

        //ヘッダ
        $contents .= implode($header, ",");
        $contents .= "\n";

        //SQL文発行 願書から紐づく募集
        $query = knjl691iQuery::getCsvData1($this->examyear, $this->applicantdiv, $this->testdiv0, $this->testdiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            $sep = ",";

            //募集データ
            $contents .= $sep."募";
            $contents .= $sep.$row["BOSYU_NAME"];
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep.$row["SOUDAN"];
            $contents .= $sep.$row["BOSYU_FS_CD"];
            $contents .= $sep.$row["BOSYU_SCHOOL"];
            $contents .= $sep.$row["COMMON_TEST"];
            $contents .= $sep.$row["KESSEKI"];
            $contents .= $sep.$row["KESSEKI_RIYUU"];
            $contents .= $sep.$row["TOKUTAI"];
            $contents .= $sep.$row["BUKATSUNO"];
            $contents .= $sep.$row["BIKOU"];
            $contents .= "\n";

            //願書データ
            $contents .= $row["SHIGAN_EXAMNO"];
            $contents .= $sep."願";
            $contents .= $sep.$row["SHIGAN_NAME"];
            $contents .= $sep.$row["SHIGAN_KANA"];
            $contents .= $sep.$row["SHIGAN_SEX"];
            $contents .= $sep;
            $contents .= $sep.$row["SHIGAN_FS_CD"];
            $contents .= $sep.$row["SHIGAN_SCHOOL"];
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= "\n";
            $data = true;
        }

        //SQL文発行 紐づかなかった募集のみ
        $query = knjl691iQuery::getCsvData2($this->examyear, $this->applicantdiv, $this->testdiv0, $this->testdiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            $sep = ",";

            //募集データ
            $contents .= $sep."募";
            $contents .= $sep.$row["NAME"];
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep.$row["SOUDAN"];
            $contents .= $sep.$row["FS_CD"];
            $contents .= $sep.$row["SCHOOL"];
            $contents .= $sep.$row["BOSYU_TEST"];
            $contents .= $sep.$row["BOSYU_KESSEKI"];
            $contents .= $sep.$row["BOSYU_KESSEKI_RIYUU"];
            $contents .= $sep.$row["TOKUTAI"];
            $contents .= $sep.$row["BOSYU_BUKATSUNO"];
            $contents .= $sep.$row["BOSYU_BIKOU"];
            $contents .= "\n";

            //願書データ
            $contents .= $sep."願";
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= $sep;
            $contents .= "\n";
            $data = true;
        }
        
        //フッター
        $contents .= ",,,,,,,※注意　募：募集データ（上段）　願：願書データ（下段）";

        $result->free();

        Query::dbCheckIn($db);
        //CSV出力
        if ($data) {
            $fname  = $this->examyear."年度 ".$testDivName."　募集・願書チェックリスト";
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
