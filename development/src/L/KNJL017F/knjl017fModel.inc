<?php
class knjl017fModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;
        $this->cmd      = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")
                              );
    }

    public function validateInput()
    {
        if (!strlen($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 学校種別 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $recCnt = $db->getOne(knjl017fQuery::selectMainQuery($this, "CNT"));
        if ($recCnt == 0) {
            $this->setMessage("MSG303", "\\n該当データは、ありません。");
            Query::dbCheckIn($db);
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //変換実行
    public function &getExecModel()
    {
        if ($this->validateInput()) {
            $ret = knjl017fQuery::executeQuery($this);
            $this->setMessage("MSG202");
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["EXAMNO"]           = "受験番号";
        $header["NAME"]             = "氏名";
        $header["MIRAI_FS_NAME"]    = "出身学校名";
        $header["MIRAI_FS_CD"]      = "学校コード";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //SQL文発行
        $query = knjl017fQuery::selectMainQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

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
        //学校種別名称
        $AppName = $db->getOne(knjl017fQuery::getNameMst($this->examyear, "L003", $this->field["APPLICANTDIV"]));
        //ファイル名
        $title = $this->examyear.'年度 '.$AppName.' 未変換リスト';

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
