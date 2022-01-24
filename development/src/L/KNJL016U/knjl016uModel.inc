<?php

require_once('for_php7.php');

class knjl016uModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        /******************/
        /*  願書（中学）  */
        /******************/
        $header = array();

        /**************/
        /*  ヘッダー  */
        /**************/
        $header["EXAMNO"]               = "受験番号";
        $header["BIRTHDAY"]             = "生年月日";
        $header["JUDGE_STATUS"]         = "合格ステータス";//101:１回目合格　201:2回目合格　301:3回目合格　99:不合格
        $header["REMARK1"]              = "備考１";
        $header["REMARK2"]              = "備考２";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        // データ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //SQL文発行
        $query = knjl016uQuery::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            //生年月日のハイフンをカット
            $row["BIRTHDAY"] = str_replace("-", "", $row["BIRTHDAY"]);

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

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl016uQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $TestdivName = $db->getOne(knjl016uQuery::getNameMst($this->ObjYear, "L004", $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestdivName.' 合格者';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
