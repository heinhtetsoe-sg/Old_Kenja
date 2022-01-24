<?php

require_once('for_php7.php');

class knjl330cModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl330cModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field["APPLICANTDIV"]    = "1";  //1:中学固定
        $this->field["TESTDIV"]         = "2";  //2:後期固定
        $this->field["SHDIV"]           = "1";  //1:専願固定

        $this->cmd = VARS::request("cmd");

        //学校コードを取得
        $rtnRow = knjl330cQuery::getNameMstZ010();
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //ヘッダー
        $header = array();
        $header["LOGIN_ID"]     = "ログインID";
        $header["PASSWORD"]     = "パスワード";
        $header["STATUS"]       = "合否ステータス";
        $header["REMARK"]       = "備考";

        $contents  = implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //出力データ作成
        $data_flg = false;
        $query = knjl330cQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if ($this->field["TESTDIV"] != "ALL" && $this->field["TESTDIV"] != $row["TESTDIV"]) {
                continue;
            }
            if ($this->field["SHDIV"] != "ALL" && $this->field["SHDIV"] != $row["SHDIV"]) {
                continue;
            }

            $row["PASSWORD"] = str_replace("-", "", $row["PASSWORD"]);

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";

            $data_flg = true;
        }
        $result->free();

        $query1 = knjl330cQuery::getNameMst($this, $this->ObjYear, "L003", $this->field["APPLICANTDIV"]);
        $query2 = knjl330cQuery::getNameMst($this, $this->ObjYear, "L004", $this->field["TESTDIV"]);
        $query3 = knjl330cQuery::getNameMst($this, $this->ObjYear, "L006", $this->field["SHDIV"]);
        $combo1 = $db->getRow($query1, DB_FETCHMODE_ASSOC);
        $combo2 = $db->getRow($query2, DB_FETCHMODE_ASSOC);
        $combo3 = $db->getRow($query3, DB_FETCHMODE_ASSOC);

        $title  = "";
        $title .= "_" . $this->ObjYear . "年度";
        $title .= "_" . $combo1["CSV_LABEL"];
        $title .= "_" . ($this->field["TESTDIV"] != "ALL" ? $combo2["CSV_LABEL"] : "全て");
        $title .= "_" . ($this->field["SHDIV"]   != "ALL" ? $combo3["CSV_LABEL"] : "全て");
        $title .= "_" . "(6桁)";

        //DB切断
        Query::dbCheckIn($db);

        if ($data_flg) {
            $couse = "合否発表用_合否マスタ" . $title;
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
