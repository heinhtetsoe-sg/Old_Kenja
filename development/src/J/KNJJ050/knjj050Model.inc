<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj050Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    function knjj050Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("DATE"         =>  VARS::post("DATE"),     //処理年月日
                             "YEAR"         =>  VARS::post("YEAR"),     //年度
                             "GAKKI"        =>  VARS::post("GAKKI"),    //学期コード
                             "FROM_DATE"    =>  VARS::post("FROM_DATE"),//対象期間From
                             "TO_DATE"      =>  VARS::post("TO_DATE"),  //対象期間To
                             "hogosya"      =>  VARS::post("hogosya"),  //保護者、住所、電話番号
                             "taibusya_nozoku"      =>  VARS::post("taibusya_nozoku"),      //退部者除く
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");

        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = "部員名簿一覧（クラス単位）";
        $wrk_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);

        $contents  = "{$wrk_nendo}　{$title},,,,,,,,,{$wrk_date}\n";

        $data = false;
        $dataTemp = array();

        if ($this->field["hogosya"] == "") {
            //ヘッダの作成//
            $header = array("HR_NAMEABBV"   => "年組",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "CLUBCD"        => "部クラブコード／名称",
                            "SDATE"         => "部活開始日",
                            "EDATE"         => "部活終了日",
                            "EXECUTIVECD"   => "役職",
                            "REMARK"        => "備考",
                            "DETAIL_DATE"   => "記録備考日付",
                            "DETAIL_REMARK" => "記録備考"
                            );

            $contents .= implode($header, ",") ."\n";

            //出力データの作成//
            $query = knjj050Query::selectCsvQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $colon = ($row["CLUBCD"] && $row["CLUBNAME"]) ? ":" : "";

                $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
                $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                $dataTemp["NAME"]           = $row["NAME"];
                $dataTemp["CLUBCD"]         = $row["CLUBCD"].$colon.$row["CLUBNAME"];
                $dataTemp["SDATE"]          = ($row["SDATE"]) ? str_replace("-","/",$row["SDATE"]) : "";
                $dataTemp["EDATE"]          = ($row["EDATE"]) ? str_replace("-","/",$row["EDATE"]) : "";
                $dataTemp["EXECUTIVECD"]    = $row["EXECUTIVECD"];
                $dataTemp["REMARK"]         = $row["REMARK"];
                $dataTemp["DETAIL_DATE"]    = ($row["DETAIL_DATE"]) ? str_replace("-","/",$row["DETAIL_DATE"]) : "";
                $dataTemp["DETAIL_REMARK"]  = $row["DETAIL_REMARK"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
            $result->free();
        } else {
            //ヘッダの作成//
            $header = array("HR_NAMEABBV"   => "年組",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "CLUBCD"        => "部クラブコード／名称",
                            "EXECUTIVECD"   => "役職",
                            "SDATE"         => "部活開始日",
                            "EDATE"         => "部活終了日",
                            "GUARD_NAME"    => "保護者氏名",
                            "GUARD_ADDR"    => "保護者住所",
                            "GUARD_TELNO"   => "保護者電話番号"
                            );

            $contents .= implode($header, ",") ."\n";

            //出力データの作成//
            $query = knjj050Query::selectCsvQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $colon = ($row["CLUBCD"] && $row["CLUBNAME"]) ? ":" : "";

                $dataTemp["HR_NAMEABBV"]    = $row["HR_NAMEABBV"];
                $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                $dataTemp["NAME"]           = $row["NAME"];
                $dataTemp["CLUBCD"]         = $row["CLUBCD"].$colon.$row["CLUBNAME"];
                $dataTemp["EXECUTIVECD"]    = $row["EXECUTIVECD"];
                $dataTemp["SDATE"]          = ($row["SDATE"]) ? str_replace("-","/",$row["SDATE"]) : "";
                $dataTemp["EDATE"]          = ($row["EDATE"]) ? str_replace("-","/",$row["EDATE"]) : "";
                $dataTemp["GUARD_NAME"]     = $row["GUARD_NAME"];
                $dataTemp["GUARD_ADDR"]     = $row["GUARD_ADDR"];
                $dataTemp["GUARD_TELNO"]    = $row["GUARD_TELNO"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        if ($data) {
            $couse = "{$wrk_nendo}　部員名簿一覧（クラス単位）";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
