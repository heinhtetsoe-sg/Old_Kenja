<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl321nModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    public function knjl321nModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    public function init()
    {
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "SCORE_TOTAL"  =>  VARS::post("SCORE_TOTAL"),    //合計点
                             "ORDER"        =>  VARS::post("ORDER")           //表示順
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //科目取得
        $testSubclass = array();
        $query = knjl321nQuery::getNameMstL009($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testSubclass[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //ヘッダデータ
        $header = array();
        $header["EXAMCOURSE_NAME"]      = "第一志望コース";
        $header["KYOIKU_NAME"]          = "教育";
        $header["SUC_EXAMCOURSE_NAME"]  = "合格コース";
        $header["SHDIV_NAME"]           = "区分";
        $header["EXAMNO"]               = "受験番号";
        $header["NAME"]                 = "生徒名";
        $header["FINSCHOOL_NAME"]       = "中学校名";
        $header["KESSEKI_JITAI_NAME"]   = "欠席辞退";
        foreach ($testSubclass as $val => $label) {
            $header["SCORE".$val]           = $label;
        }
        $header["TOTAL4"]               = "合計";
        $header["DIV_RANK4"]            = "順位";
        $header["EXAMCOURSE_NAME2"]     = "第二志望コース";
        $header["REMARK1"]              = "備考1";
        $header["REMARK2"]              = "備考2";
        $header["REMARK3"]              = "備考3";
        $header["REMARK4"]              = "備考4";
        $header["REMARK6"]              = "備考6";
        $header["CLUB_NAME"]            = "クラブ";
        $header["TOTAL_ALL"]            = "評定合計";
        $header["TOTAL5"]               = "5科目評定合計";
        $header["KASANTEN_ALL"]         = "段階";

        $contents  = "";    //クリア
        $header_data = true;   //出力フラグ

        //入試制度
        $l003Row = $db->getRow(knjl321nQuery::getNameMst($this, "L003", $this->field["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
        //入試区分
        $l004Row = $db->getRow(knjl321nQuery::getNameMst($this, "L004", $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
        //合計点
        $totalName = strlen($this->field["SCORE_TOTAL"]) ? "(合計点".$this->field["SCORE_TOTAL"]."点以下)" : "";
        //表示順
        $orderName = $this->field["ORDER"] == "1" ? "(成績順)" : "(受験番号順)";
        $dateStr = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);

        //ヘッダ1行目
        $header1 = $this->ObjYear."年度".$l003Row["CSV_NAME"].$l004Row["CSV_NAME"].$totalName."　　".$dateStr;
        $contents .= $header1."\n";

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjl321nQuery::selectMainQuery($this, $testSubclass);

        $dataTemp = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);
            //備考1
            $row["REMARK7_NAME"] = !strlen($row["REMARK7_NAME"]) ? "" : $row["REMARK7_NAME"];
            $row["REMARK1"] = !strlen($row["REMARK1"]) ? "" : "(".$row["REMARK1"].")";
            $row["REMARK1"] = $row["REMARK7_NAME"].$row["REMARK1"]; //備考1
            //得点
            foreach ($testSubclass as $val => $label) {
                $row["SCORE".$val] = $row["ATTEND_FLG".$val] === "0" ? "*" : $row["SCORE".$val];
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "成績一覧表CSV";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
