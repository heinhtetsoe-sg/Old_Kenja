<?php

require_once('for_php7.php');

class knjl030vModel extends Model
{
    public $cmd;
    public $examyear;
    public $schoolKind;
    public $examId;

    public function init()
    {
        $this->cmd              = VARS::request("cmd");
        $this->examyear         = CTRL_YEAR + 1;
        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->examId           = VARS::post("EXAM_ID");

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            //機能別に指定された校種を取得
            $this->getSelectSchoolKind();
        }
    }

    //実行
    public function getExecModel()
    {
        knjl030vQuery::getExecQuery($this);
        $this->setMessage("MSG202");
    }

    //CSV出力
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["YEAR"]             = "年度";
        $header["EXAM_ID"]          = "試験ID";
        $header["EXAM_NAME"]        = "試験名";
        $header["RECEPTNO"]         = "受験番号";
        $header["NAME"]             = "氏名";
        $header["EXAM_SUBCLASS"]    = "科目コード";
        $header["SUMMARY_DIV"]      = "集計種別";
        $header["EXAM_SCORE"]       = "得点";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //試験名
        $examMst = array();
        $examMst = $db->getRow(knjl030vQuery::getExamId($this, $this->examId), DB_FETCHMODE_ASSOC);

        //データ出力
        $dataFlg = false;
        $query = knjl030vQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["YEAR"] = $this->examyear;
            $row["EXAM_ID"] = str_replace("-", "", $this->examId);
            $row["EXAM_NAME"] = $examMst["EXAM_NAME"];

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            $title = $this->examyear."年度".$examMst["EXAM_NAME"]."試験集計データ";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
