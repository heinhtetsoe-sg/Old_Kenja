<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl305rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    /**
     * メンバ変数の初期化
     */
    function knjl305rModel()
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
                             "OUTPUT"      =>  VARS::post("OUTPUT")           //出力順
                     );

        $this->cmd         = VARS::request("cmd");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }
    
    //CSV出力処理
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $testdivabbv1 = "";
        $testdivabbv2 = "";
        $query = knjl305rQuery::getNameMstVal($this->ObjYear, "ABBV1", "L004", $this->field["TESTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivabbv1 = $row["ABBV1"];
        }
        $query = knjl305rQuery::getNameMstVal($this->ObjYear, "ABBV2", "L004", $this->field["TESTDIV"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivabbv2 = $row["ABBV2"];
        }

        //ヘッダ配列
        $header1 = array();
        $header1[]= $testdivabbv1."願書";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= $testdivabbv2;
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        $header1[]= "";
        if ($this->field["TESTDIV"] == '1') {
            $header1[]= "";
        }
        $header1[]= "";
        $header1[]= "";
        if ($this->field["TESTDIV"] == '2') {
            $header1[]= "";
        }

        $header2 = array();
        $header2[]= "受験番号";
        $header2[]= "姓(カタカナ)";
        $header2[]= "名(カタカナ)";
        $header2[]= "性別";
        $header2[]= "出身校コード";
        $header2[]= "出身学校名";
        $header2[]= "コース１";
        $header2[]= "コース２";
        $header2[]= "コース３";
        $header2[]= "コース４";
        $header2[]= "ページ";
        $header2[]= "番号";
        $header2[]= "姓(カタカナ)";
        $header2[]= "名(カタカナ)";
        $header2[]= "性別";
        $header2[]= "内諾コース";
        $header2[]= "サブ";
        $header2[]= "難関コース希望";
        if ($this->field["TESTDIV"] == '1') {
            $header2[]= "備考";
        }
        $header2[]= "出身校コード";
        $header2[]= "出身学校名";
        if ($this->field["TESTDIV"] == '2') {
            $header2[]= "選抜Ⅰ合格";
        }

        //ヘッダ配列を出力変数にセット
        $headerContents .= implode($header1, $comma) .$lf;
        $headerContents .= implode($header2, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        for ($i = 0; $i < 3; $i++) {
            $query = knjl305rQuery::getCsvQuery($this->ObjYear, $this, $i);
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
                $dataContents .= $row["BASE_SEX_NAME"].$comma;
                $dataContents .= $row["BASE_FS_CD"].$comma;
                $dataContents .= $row["BASE_FINSCHOOL_NAME"].$comma;
                $dataContents .= $row["BASE_EXAM_COURSEMARK1"].$comma;
                $dataContents .= $row["BASE_EXAM_COURSEMARK2"].$comma;
                $dataContents .= $row["BASE_EXAM_COURSEMARK3"].$comma;
                $dataContents .= $row["BASE_EXAM_COURSEMARK4"].$comma;
                $dataContents .= $row["BEFORE_PAGE"].$comma;
                $dataContents .= $row["BEFORE_SEQ"].$comma;
                $idx = strpos($row["BEF_NAME_KANA"], '　');
                if ($idx === false) {
                    $dataContents .= $row["BEF_NAME_KANA"].$comma;
                    $dataContents .= ''.$comma;
                } else {
                    $dataContents .= substr($row["BEF_NAME_KANA"], 0, $idx).$comma;
                    $dataContents .= substr($row["BEF_NAME_KANA"], $idx + 1).$comma;
                }
                $dataContents .= $row["BEF_SEX_NAME"].$comma;
                $dataContents .= $row["BEF_COURSEMARK"].$comma;
                $dataContents .= $row["BEF_SUB"].$comma;
                $dataContents .= $row["BEF_NANKAN_FLG"].$comma;
                if ($this->field["TESTDIV"] == '1') {
                    $dataContents .= $row["BEF_RECOM_FLG_NAME"].((''  == $row["BEF_RECOM_FLG"] || '' == $row["BEF_RECOM_REMARK"]) ? "" : "（" + $row["BEF_RECOM_REMARK"] + "）").$row["BEF_REMARK"].$comma;
                }
                $dataContents .= $row["BEF_FS_CD"].$comma;
                $dataContents .= $row["BEF_FINSCHOOL_NAME"].$comma;
                if ($this->field["TESTDIV"] == '2') {
                    $text = $row["BEF_SENBATU1_SCHOOL"];
                    if ('1' == $row["BEF_SENBATU1"]) {
                        $text .= "（合格）";
                    }
                    $dataContents .= $text.$comma;
                }
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
        $title = $nendo.'　'.$testdivabbv1.'入試願書　'.$testdivabbv2.'照会リスト'.$lf;
        //サブタイトル
        $subtitle = "";
        //$subtitle  =  $comma.$comma.$comma.$comma.$comma."";
        //$subtitle .=  $comma.$comma.$comma.$this->wareki_date.$lf;
        
        $contents  = "";
        $contents .= $title;
        $contents .= $subtitle;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $nendo.'　'.$testdivabbv1.'願書　'.$testdivabbv2.'照会リスト');
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
