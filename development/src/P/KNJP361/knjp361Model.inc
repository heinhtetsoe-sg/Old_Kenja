<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp361Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;                  //コントロールマスタ
    var $staffcd;                  //職員コード
    var $reduc;                    //特殊コード NO001
    var $reductionmoney;           //補助金 NO002
    var $class_select = array();   //クラス NO002
    var $output;
        
    /**
     * メンバ変数の初期化
     */
    function knjp361Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;    //職員コード
    }
    function init()
    {
        $this->cmd            = VARS::request("cmd");
        $this->reduc          = VARS::post("REDUC_RARE_CASE_CD");                    //NO001
        $this->reductionmoney = VARS::post("REDUCTIONMONEY");                        //NO002
        $this->class_select["selectdata"] = explode(",", VARS::post("selectdata"));  //NO002
        $this->output         = VARS::request("OUTPUT");                             //帳票種別ラジオ
    }
    /**
     * 変数をクリアします
     */
    function getDownloadModel() {
        if ($this->output == 2) {
            return $this->printFuken();
        }else{
            return $this->printKuni();
        }
    }

    /**
     * CSV出力（府県）
     */
    function printFuken() {
        //年度表示
        $date = explode('-', CTRL_DATE);
        $date[0] = $date[0] - 1;
        $pre_date = implode('/',$date);
        $wareki1 = common::DateConv1($pre_date,2)."度";
        $wareki2 = common::DateConv1(str_replace("-","/",CTRL_DATE),2)."度";

        $header = array("GRADE"                 => "学年",
                        "HR_CLASS"              => "組",
                        "ATTENDNO"              => "出席番号",
                        "PASSNO"                => "認定番号",
                        "NAME"                  => "生徒氏名",
                        "GUARANTOR_NAME"        => "学費負担者名",
                        "REDUC_RARE_CASE_CD"    => "特殊",
                        "REDUC_INCOME_1"        => $wareki1."所得割額",
                        "REDUC_RANK_1_NAME"     => "ランク１",
                        "REDUCTIONMONEY_1"      => "府県補助額１",
                        "REDUC_INCOME_2"        => $wareki2."所得割額",
                        "REDUC_RANK_2_NAME"     => "ランク２",
                        "REDUCTIONMONEY_2"      => "府県補助額２",
                        "REDUCTIONMONEY_TOTAL"  => "府県補助額合計",
                        "PREF"                  => "都道府県",
                        "TOTAL"                 => "総合計",
                        "REDUC_REMARK"          => "備考"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjp361Query::getCsvQueryFuken($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //支援額１または支援額２がある場合、支援額計を表示
            $row["REDUCTIONMONEY_TOTAL"] = ($row["REDUCTIONMONEY_1"] != "" || $row["REDUCTIONMONEY_2"] != "") ? $row["REDUCTIONMONEY_TOTAL"] : "";
            //データ設定
            $contents .= $row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["PASSNO"].","
                        .$row["NAME"].","
                        .$row["GUARANTOR_NAME"].","
                        .$row["REDUC_RARE_CASE_CD"].","
                        .$row["REDUC_INCOME_1"].","
                        .$row["REDUC_RANK_1_NAME"].","
                        .$row["REDUCTIONMONEY_1"].","
                        .$row["REDUC_INCOME_2"].","
                        .$row["REDUC_RANK_2_NAME"].","
                        .$row["REDUCTIONMONEY_2"].","
                        .$row["REDUCTIONMONEY_TOTAL"].","
                        .$row["PREF"].","
                        .$row["TOTAL"].","
                        .$row["REDUC_REMARK"].","
                        ."\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if($data){
            $filename = $wareki2."_"."授業料軽減補助金（府県補助金）.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }

    /**
     * CSV出力（国）
     */
    function printKuni() {
        //年度表示
        $date = explode('-', CTRL_DATE);
        $date[0] = $date[0] - 1;
        $pre_date = implode('/',$date);
        $wareki1 = common::DateConv1($pre_date,2)."度";
        $wareki2 = common::DateConv1(str_replace("-","/",CTRL_DATE),2)."度";

        $header = array("GRADE"                     => "学年",
                        "HR_CLASS"                  => "組",
                        "ATTENDNO"                  => "出席番号",
                        "PASSNO"                    => "認定番号",
                        "NAME"                      => "生徒氏名",
                        "GUARANTOR_NAME"            => "学費負担者名",
                        "REDUC_RARE_CASE_CD_1"      => "特殊フラグ１",
                        "REDUC_INCOME_1"            => $wareki1."所得割額",
                        "PLAN_MONEY1"               => "支援金基本額1",
                        "ADD_PLAN_MONEY1"           => "加算額１",
                        "REDUC_RARE_CASE_CD_2"      => "特殊フラグ２",
                        "REDUC_INCOME_2"            => $wareki2."所得割額",
                        "PLAN_MONEY2"               => "支援金基本額2",
                        "ADD_PLAN_MONEY2"           => "加算額２",
                        "ADD_PLAN_MONEY_TOTAL"      => "加算額合計",
                        "PLAN_MONEY_TOTAL"          => "支援額合計",
                        "PREF"                      => "都道府県",
                        "REDUC_REMARK"              => "備考"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjp361Query::getCsvQueryKuni($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //加算額１または加算額２がある場合、加算額合計を表示
            if ($row["ADD_PLAN_MONEY1"] != "" || $row["ADD_PLAN_MONEY2"] != "") {
            } else {
                $row["ADD_PLAN_MONEY_TOTAL"] = "";
            }
            //加算額１または加算額２または支援金基本額がある場合、支援額合計を表示
            if ($row["ADD_PLAN_MONEY1"] != "" || $row["ADD_PLAN_MONEY2"] != "" || $row["PLAN_MONEY"] != "") {
            } else {
                $row["PLAN_MONEY_TOTAL"] = "";
            }
            //データ設定
            $contents .= $row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["PASSNO"].","
                        .$row["NAME"].","
                        .$row["GUARANTOR_NAME"].","
                        .$row["REDUC_RARE_CASE_CD_1"].","
                        .$row["REDUC_INCOME_1"].","
                        .$row["PLAN_MONEY1"].","
                        .$row["ADD_PLAN_MONEY1"].","
                        .$row["REDUC_RARE_CASE_CD_2"].","
                        .$row["REDUC_INCOME_2"].","
                        .$row["PLAN_MONEY2"].","
                        .$row["ADD_PLAN_MONEY2"].","
                        .$row["ADD_PLAN_MONEY_TOTAL"].","
                        .$row["PLAN_MONEY_TOTAL"].","
                        .$row["PREF"].","
                        .$row["REDUC_REMARK"].","
                        ."\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if($data){
            $filename = $wareki2."_"."授業料軽減補助金（国就学支援金）.csv";
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
