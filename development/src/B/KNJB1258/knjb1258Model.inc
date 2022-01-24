<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb1258Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjb1258Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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

        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER")
                            );

        $this->getPropertiesAll(); //プロパティファイル
    }
    function &getMainModel()
    {
    }

    // 受講クラスの自動生成
    function getDownloadModel2()
    {
        // ＣＳＶ項目
        $header = array("YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "CHAIRCD"       => "※講座コード",
                        "GROUPCD"       => "※群コード",
                        "TRGTGRADE"     => "※対象年",
                        "TRGTCLASS"     => "※対象組");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        // 0:HRな講座 1:群
        list($year, $semester) = explode(':', $this->field["YEAR_SEMESTER"]);
        for ($flg = 0; $flg < 2; $flg++) {
            $query = knjb1258Query::selectAutoChairCls($year, $semester, $flg);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                // DB追加
                $query = knjb1258Query::insertAutoChairCls($row, $year, $semester, STAFFCD, $flg);
                $db->query($query);
                // CSV出力
                $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= $year .",";
                $contents .= $semester .",";
                if ($flg == 0) {
                    $contents .= $row["CHAIRCD"] .",";
                    $contents .= "0000" .",";
                } else {
                    $contents .= "0000000" .",";
                    $contents .= $row["GROUPCD"] .",";
                }
                $contents .= $row["GRADE"] .",";
                $contents .= $row["HR_CLASS"] ."\n";
                $header_data = true;
            }
            $result->free();
        }

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv","講座受講クラスデータ(自動生成)");
                common::downloadContents($contents, $filename);
            } else {
                $this->warning = "データを更新しました。";
                return false;
            }
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
//var_dump($_REQUEST);
?>
