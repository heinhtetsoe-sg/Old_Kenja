<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl305fModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl305fModel()
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
                             "TESTDIV0"     =>  VARS::post("TESTDIV0"),       //入試回数
                             "OUTPUT"       =>  VARS::post("OUTPUT")          //出力順
                     );

        $this->cmd         = VARS::request("cmd");
    }
    
    //CSV出力処理
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $applicantdivName1 = "";
        $testdivKotei = "";
        $query = knjl305fQuery::getNameMstVal($this->ObjYear, "NAME1", "L003", $this->field["APPLICANTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $applicantdivName1 = $row["NAME1"];
        }
        $query = knjl305fQuery::getTestDivKoteiVal($this->field["TESTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivKotei = $row["LABEL"];
        }

        $testdivabbv1 = "";
        $testdivabbv2 = "";
        $query = knjl305fQuery::getNameMstVal($this->ObjYear, "ABBV1", "L004", $this->field["TESTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivabbv1 = $row["ABBV1"];
        }
        $query = knjl305fQuery::getNameMstVal($this->ObjYear, "ABBV2", "L004", $this->field["TESTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivabbv2 = $row["ABBV2"];
        }

        //ヘッダ配列
        $header1 = array();
        $header1[]= "入試願書";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "事前相談";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";

        $header2 = array();
        $header2[]= "受験番号";
        $header2[]= "姓(ひらがな)";
        $header2[]= "名(ひらがな)";
        $header2[]= "出身校コード";
        $header2[]= "出身学校名";
        $header2[]= "コース";
        $header2[]= "事前番号";
        $header2[]= "姓(ひらがな)";
        $header2[]= "名(ひらがな)";
        $header2[]= "出身校コード";
        $header2[]= "出身学校名";
        $header2[]= "特待生";

        //ヘッダ配列を出力変数にセット
        $headerContents .= implode($header1, $comma) .$lf;
        $headerContents .= implode($header2, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        for ($i = 0; $i < 3; $i++) {
            $query = knjl305fQuery::getCsvQuery($this->ObjYear, $this, $i);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataContents .= $row["BASE_EXAMNO"].$comma;
                $idx = strpos($row["BASE_NAME_KANA"], '　');
                if ($idx === false) {
                    $dataContents .= $row["BASE_NAME_KANA"].$comma;
                    $dataContents .= ''.$comma;
                } else {
                    $dataContents .= substr($row["BASE_NAME_KANA"], 0, $idx).$comma;
                    $dataContents .= substr($row["BASE_NAME_KANA"], $idx + 1).$comma;
                }
                $dataContents .= $row["BASE_FS_CD"].$comma;
                $dataContents .= $row["BASE_FINSCHOOL_NAME"].$comma;
                $dataContents .= $row["BASE_EXAM_COURSEMARK1"].$comma;
                $dataContents .= $row["RECRUIT_NO"].$comma;
                $idx = strpos($row["BEF_NAME_KANA"], '　');
                if ($idx === false) {
                    $dataContents .= $row["BEF_NAME_KANA"].$comma;
                    $dataContents .= ''.$comma;
                } else {
                    $dataContents .= substr($row["BEF_NAME_KANA"], 0, $idx).$comma;
                    $dataContents .= substr($row["BEF_NAME_KANA"], $idx + 1).$comma;
                }
                $dataContents .= $row["BEF_FS_CD"].$comma;
                $dataContents .= $row["BEF_FINSCHOOL_NAME"].$comma;
                $dataContents .= $row["BEF_JUDGENAME"].$comma;
                $dataContents .= $lf;
                $data = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $nendo = common::DateConv1($this->ObjYear."/04/01",10);
        $title = "";
        if ($this->field["APPLICANTDIV"] == "1") {
            $title = $nendo.'　'.$applicantdivName1.$testdivKotei.'入試願書　照会リスト';
        } else {
            $title = $nendo.'　'.$applicantdivName1.$testdivabbv1.'入試願書　'.$testdivabbv2.'照会リスト';
        }
        //サブタイトル
        $subtitle = "";
        
        $contents  = "";
        $contents .= $title.$lf;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
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
