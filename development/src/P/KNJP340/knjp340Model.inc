<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp340Model extends Model {
    var $field      = array();
    var $cmd;
    var $output;
    var $printPref;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $outdiv;
    var $paidYearMonth;
    var $class_select = array();   //クラス
    var $fukenBurden;
    var $ryohouBurden;

    /**
     * メンバ変数の初期化
     */
    function knjp340Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }
    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->output  = VARS::request("OUTPUT");
        $this->printPref = VARS::request("PRINT_PREF");
        $this->outdiv  = VARS::request("OUT_DIV");
        $this->paidYearMonth  = VARS::request("PAID_YEARMONTH");
        $this->fukenBurden  = VARS::request("FUKEN_BURDEN");
        $this->ryohouBurden  = VARS::request("RYOHOU_BURDEN");
        $this->class_select["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->grdDate = VARS::post("GRD_DATE");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV出力
     */
    function getDownloadModel() {
        //年度表示
        $date = explode('-', CTRL_DATE);
        $date[0] = $date[0] - 1;
        $pre_date = implode('/',$date);
        $wareki1 = common::DateConv1($pre_date,2)."度";
        $wareki2 = common::DateConv1(str_replace("-","/",CTRL_DATE),2)."度";

        $header = array("GRADE"                     => "年",
                        "HR_CLASS"                  => "組",
                        "ATTENDNO"                  => "出席番号",
                        "SLIPNO"                    => "伝票番号",
                        "SCHOOLCD"                  => "学校コード",
                        "DIVISION"                  => "区分",
                        "GAKUBU"                    => "学部",
                        "BANK_MAJORCD"              => "学科",
                        "BANK_GRADE"                => "学年",
                        "BANK_HR_CLASS"             => "学級",
                        "SCHREGNO"                  => "学生番号",
                        "NAME"                      => "生徒氏名",
                        "PROCESS"                   => "処理区分",
                        "EXPENSE"                   => "費目番号",
                        "REDU_WAY"                  => "軽減方法",
                        "REDU_START"                => "軽減開始期",
                        "REDUCTIONMONEY"            => "補助金",
                        "PREF"                      => "備考"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //変数
        $isKuniOnly  = ($this->outdiv == "1") ? true : false;
        $isFukenOnly = ($this->outdiv == "2") ? true : false;
        $isRyohou    = ($this->outdiv == "3") ? true : false;
        $month3Flg = substr($this->paidYearMonth, 4) == "03" ? true : false;
        $month = substr($this->paidYearMonth, 4);
        $reduStart = "03";
//        $reduStart = $isFukenOnly ? "04" : "";
//        if (!$isFukenOnly) {
//            if ($month == "04") $reduStart = "01";
//            if ($month == "09") $reduStart = "02";
//            if ($month == "12") $reduStart = "04";
//            if ($month == "03") $reduStart = "";
//        }
        //SQL文発行
        $query = knjp340Query::getCsvQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //固定値
            $row["SLIPNO"]      = "29";
            $row["SCHOOLCD"]    = "0245";
            $row["DIVISION"]    = "6";
            $row["GAKUBU"]      = "";
            $row["PROCESS"]     = "2";
            $row["EXPENSE"]     = "01";
            $row["REDU_WAY"]    = "1";
            $row["REDU_START"]  = $reduStart;
            //備考
            $row["PREF"]        = ($this->printPref != "") ? $row["PREF"] : "";
            $row["REDUCTIONMONEY"] += $this->fukenBurden || $this->ryohouBurden ? $row["OVER_MONEY"] : 0;
            //データ設定
            $contents .= $row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["SLIPNO"].","
                        .$row["SCHOOLCD"].","
                        .$row["DIVISION"].","
                        .$row["GAKUBU"].","
                        .$row["BANK_MAJORCD"].","
                        .$row["GRADE"].","
                        .$row["BANK_HR_CLASS"].","
                        .$row["SCHREGNO"].","
                        .$row["NAME"].","
                        .$row["PROCESS"].","
                        .$row["EXPENSE"].","
                        .$row["REDU_WAY"].","
                        .$row["REDU_START"].","
                        .$row["REDUCTIONMONEY"].","
                        .$row["PREF"].","
                        ."\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $subTitle = "";
            if ($isKuniOnly) {
                $subTitle = "国就学支援金";
            } else if ($isFukenOnly) {
                $subTitle = "府県補助金";
            } else {
                $subTitle = "国府県合計";
            }
            $filename = $wareki2."_"."授業料軽減報告票（".$subTitle."）.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
