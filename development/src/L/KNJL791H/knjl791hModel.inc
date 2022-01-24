<?php
class knjl791hModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;

    public function knjl791hModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"              =>  VARS::post("TESTDIV"),          //入試区分
                             "OUTPUT"               =>  VARS::post("OUTPUT"),           //送付先
                             "STATUS_PASS"          =>  VARS::post("STATUS_PASS"),      //ステータスコード 合格者
                             "STATUS_UNPASS"        =>  VARS::post("STATUS_UNPASS"),    //ステータスコード 不合格者
                             "HEADER"               =>  VARS::post("HEADER")            //ヘッダ
                            );
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        if ($this->field["OUTPUT"] == "1") {
            //合否サイト
            $header["EXAMNO"]       = "ログインID";
            $header["PASSWORD"]     = "パスワード";
            $header["STATUS"]       = "ステータス";
            $header["REMARK1"]      = "備考1";
            $header["REMARK2"]      = "備考2";
        } else {
            //手続サイト
            $header["EXAMNO"]       = "受験番号";
            $header["PASSWORD"]     = "任意項目";
            $header["STATUS"]       = "ステータス";
        }

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl791hQuery::SelectQueryCsv($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                if ($field == "PASSWORD") {
                    if ($this->field["OUTPUT"] == "1") {
                        $row[$field] = str_replace("-", "", $row[$field]);
                    } else {
                        $row[$field] = "";
                    }
                }
                if ($field == "STATUS") {
                    $val = $this->field["STATUS_UNPASS"];
                    if ($row["NAMESPARE1"] == "1") {
                        $val = $this->field["STATUS_PASS"];
                    }
                    $row[$field] = $val;
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->examyear."年度";
            if ($this->field["OUTPUT"] == "1") {
                $title .= "合否サイト向けアップロードデータ";
            } else {
                $title .= "手続サイト向けアップロードデータ";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
