<?php
/**
 * 定数定義
*/

class knjh081Model extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjh081Model()
    {
    }

    function init()
    {
        $this->field = array("SDATE" =>  VARS::post("SDATE"), //指導日付開始
                             "EDATE" =>  VARS::post("EDATE"), //指導日付終了
                            );

        $this->cmd = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getCsvModel() {
        
        $this->field["SDATE"] = str_replace('/', '-', $this->field["SDATE"]);
        $this->field["EDATE"] = str_replace('/', '-', $this->field["EDATE"]);

        //ヘッダデータ
        $header = array("SDATE"           => "指導日付開始",
                        "EDATE"           => "指導日付終了",
                        "YEAR"            => "年度",
                        "TRAINDATE"       => "指導日付",
                        "HR_NAME"         => "年組名称",
                        "ATTENDNO"        => "出席番号",
                        "SCHREGNO"        => "学籍番号",
                        "NAME"            => "生徒氏名",
                        "PATIENT_NAME"    => "相談者",
                        "HOWTOTRAINCD"    => "指導方法コード",
                        "HOWTOTRAIN_NAME" => "指導方法",
                        "CONTENT"         => "指導内容",
                        "STAFFCD"         => "教職員番号",
                        "STAFFC_NAME"     => "教職員氏名"
                        );

        $mojisu = array();
        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjh081Query::selectMainQuery($this);

        //SQL文発行
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
            $couse = "服装頭髪指導";
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
