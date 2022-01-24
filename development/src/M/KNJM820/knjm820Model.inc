<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm820Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm820Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }


    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = ""; //CSVデータ出力用
        $data = false;
        $query = knjm820Query::getCsvQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $contents .= $row["SCHREGNO"];
            $contents .= ",".$row["NAME"];
            $contents .= ",".$row["SEX_NAME"];
            $contents .= ",".$row["SEND_ZIPCD"];
            $contents .= ",".$row["SEND_ADDR1"];
            $contents .= ",".$row["SEND_ADDR2"];
            $contents .= ",".$row["SEND_TELNO"];
            $contents .= "\n";

            $data = true;
        }
        $result->free();


        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $filename = CTRL_YEAR."年度_除籍候補者一覧.csv";
            $header = "学籍番号,氏名,性別,郵便番号,住所1,住所2,電話番号\n";
            common::downloadContents($header.$contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
