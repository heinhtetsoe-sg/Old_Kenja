<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl607fModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $applicantdivName;
    var $testdivName;

    /**
     * メンバ変数の初期化
     */
    function knjl607fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "EXAMNO_FROM"  =>  VARS::post("EXAMNO_FROM"),    //受験番号FROM
                             "EXAMNO_TO"    =>  VARS::post("EXAMNO_TO")       //受験番号TO
                     );

        $this->cmd         = VARS::request("cmd");
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    
    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //入試制度名称
        $this->applicantdivName = $db->getOne(knjl607fQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));

        //入試区分名称
        $namecd1 = ($this->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $this->testdivName = $db->getOne(knjl607fQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/

        $nendo = $this->ObjYear."/04/01";
        $nendo = common::DateConv1($nendo,10);  //年度
        $ctrl_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);   //作成日

        $title = $nendo.' '.$this->applicantdivName.' '.$this->testdivName.' 志願者情報確認表';
        $header1 = array('','','','','','',$title);
        $header2 = array('','','','','','','','','','','','','','','','','','','','','','','','','',$ctrl_date);

        //項目ヘッダ
        $header = array();
        $header["RECEPTDATE"]           = "受付日付";
        $header["EXAMNO"]               = "受験番号";
        $header["NAME"]                 = "受験者氏名";
        $header["NAME_KANA"]            = "氏名かな";
        $header["SEX_NAME"]             = "性別";
        $header["BIRTHDAY"]             = "生年月日";
        $header["FS_CD"]                = "学校コード";
        $header["FINSCHOOL_NAME"]       = "出身学校名";
        // $header["FINSCHOOL_ZIPCD"]      = "";
        $header["ZIPCD"]                = "郵便番号";
        $header["ADDRESS1"]             = "住所1";
        $header["ADDRESS2"]             = "住所2";
        $header["FS_GRD_DIV_NAME"]      = "現浪";
        $header["AVERAGE_ALL"]          = "評定平均";
        $header["CONFIDENTIAL_RPT01"]   = "国語";
        $header["CONFIDENTIAL_RPT02"]   = "社会";
        $header["CONFIDENTIAL_RPT03"]   = "数学";
        $header["CONFIDENTIAL_RPT04"]   = "理科";
        $header["CONFIDENTIAL_RPT05"]   = "音楽";
        $header["CONFIDENTIAL_RPT06"]   = "美術";
        $header["CONFIDENTIAL_RPT07"]   = "保険";
        $header["CONFIDENTIAL_RPT08"]   = "技術";
        $header["CONFIDENTIAL_RPT09"]   = "外国";
        $header["EIKEN_NAME"]           = "英検級数";
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["DETAIL1"]     = "プレテスト1";
            $header["DETAIL2"]     = "プレテスト2";
        } else {
            $header["DETAIL1"]     = "実力";
            $header["DETAIL2"]     = "事前相談結果";
        }
        $header["SHOUGAKU_NAME"]        = "奨学区分";
        $header["DETAIL5_REMARK4"]      = "備考";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        //ヘッダ
        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $contents .= implode($header, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //データ
        $query = knjl607fQuery::selectDataQuery($this);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"),"",str_replace(",",".",$row));
            $setArray = $header;

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($setArray as $field => $label) {
                if ($field == "RECEPTDATE" || $field == "BIRTHDAY"){
                    $val = str_replace("-","．",$row[$field]);
                    $contents .= $sep.$val;
                } else {
                    $contents .= $sep.$row[$field];
                }
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $title = $nendo.' '.$this->applicantdivName.' '.$this->testdivName.' 志願者情報確認表';
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
