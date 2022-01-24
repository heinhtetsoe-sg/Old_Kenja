<?php

require_once('for_php7.php');

class knjm262wModel extends Model
{
    public $field = array();
    public $cmd;

    public function knjm262wModel()
    {
    }

    public function init()
    {
        $this->field = array("SDATE"            =>  VARS::post("SDATE"),            //開始日付
                             "EDATE"            =>  VARS::post("EDATE")             //終了日付
                            );

        $this->cmd = VARS::request("cmd");
    }

    //CVSファイル
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        //ヘッダー
        $header  = array();
        $header["STAFFNAME_SHOW"] = "教員名";
        $header["SUBCLASSNAME"] = "科目名";
        $header["COUNT"] = "受付件数";
        $header["REMARK"] = "備考";

        $contents = ""; //クリア
        $data = false; //出力フラグ

        //ヘッダ
        $contents .= $this->field["SDATE"]." ～ ".$this->field["EDATE"]." 受付分,,,";
        $contents .= "作成日 ".str_replace("-", "/", CTRL_DATE);
        $contents .= "\n";
        $contents .= implode($header, ",");
        $contents .= "\n";

        //データ
        $sDate = str_replace("/", "-", $this->field["SDATE"]);
        $eDate = str_replace("/", "-", $this->field["EDATE"]);
        $query = knjm262wQuery::getCsvQuery($sDate, $eDate);

        //SQL文発行
        $result = $db->query($query);
        
        $staffName = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                if ("ALL" == $row["STAFFCD"] && "SUBCLASSNAME" == $field) {
                    $contents .= $sep."合計";
                } elseif ("STAFFNAME_SHOW" == $field) {
                    if ($staffName != $row[$field]) { //名前は最初だけ出力
                        $contents .= $sep.$row[$field];
                        $staffName = $row[$field];
                    }
                } else {
                        $contents .= $sep.$row[$field];
                }

                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $fname  = CTRL_YEAR."年度";
            $fname .= " レポート受付通数集計表";
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
