<?php

require_once('for_php7.php');

class knjl091oModel extends Model {
    var $cmd;
    var $examyear;

    function knjl091oModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->field = array("APPLICANTDIV" => VARS::post("APPLICANTDIV"),
                             "TESTDIV"      => VARS::post("TESTDIV")
                            );
    }

    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $data = $this->getDownloadStudent($db, $contents);

        if ($data) {
            //CSV出力
            $this->putCsv($db, $contents);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSV出力
    function putCsv($db, $contents)
    {
        $filename = $this->examyear."年度合格結果.csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //合格者
    function getDownloadStudent($db, &$contents)
    {
        $header = array("LOGINID"           => "ログインID",
                        "PASSWD"            => "パスワード",
                        "STATUS"            => "合否ステータス",
                        "REMARK"            => "備考",
                        "REMARK2"           => "備考２");

        $contents = implode($header, ",")."\n";
        $data = false;

        //SQL文発行
        $query = knjl091oQuery::selectCvsQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $data = true;
        }
        $result->free();

        return $data;
    }
}
?>
