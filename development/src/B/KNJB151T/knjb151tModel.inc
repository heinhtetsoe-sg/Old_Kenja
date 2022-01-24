<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb151tModel extends Model {
    var $field       = array(); //フィールド
    var $cmd;                   //コマンド
    var $control;               //コントロールマスタ
    var $staffcd;               //職員コード
    var $select_data = array(); //CSV対象データ
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjb151tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        //コマンド
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        //CSV対象データ
        $this->select_data = explode(",", VARS::post("selectdata"));
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        $header = array("YEAR"              => "年度",
                        "SEMESTER"          => "学期",
                        "HR_ATTEND"         => "年-組-番",
                        "SCHREGNO"          => "学籍番号",
                        "NAME_SHOW"         => "氏名",
                        "TEXTBOOKCD"        => "教科書コード",
                        "TEXTBOOKNAME"      => "教科書名",
                        "ISSUECOMPANYNAME"  => "発行社",
                        "TEXTBOOKPRICE"     => "定価",
                        "CHAIRCD"           => "講座コード",
                        "CHAIRNAME"         => "講座名",
                        "YOUFUYOU"          => "要/不要");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //対象生徒
        $inSentence = "";
        $sep = "";
        for ($i = 0; $i < get_count($this->select_data); $i++) {
            $inSentence .= $sep."'".$this->select_data[$i]."'";
            $sep = ",";
        }
        //データ取得
        $query = knjb151tQuery::getCsvData($inSentence);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "生徒別購入表データ";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
}
?>
