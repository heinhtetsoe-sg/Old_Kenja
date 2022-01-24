<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl301rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    /**
     * メンバ変数の初期化
     */
    function knjl301rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
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

        //ヘッダ配列
        $header = array("入試区分",
                        "会場名",
                        "受験番号",
                        "氏名",
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        $query = knjl301rQuery::getCsvQuery($this->ObjYear, $this);
        $testdivName1 = '';
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivName1 = $row["TESTDIV_NAME"];
            $dataContents .= $row["TESTDIV_NAME"] . $comma;
            $dataContents .= $row["EXAMHALL_NAME"] . $comma;
            $dataContents .= $row["EXAMNO"] . $comma;
            $dataContents .= $row["NAME"] . $comma;
            $dataContents .= $lf;
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $nendo = common::DateConv1($this->ObjYear."/04/01",10);
        $title = $nendo.'　'.$testdivName1.'会場ラベルデータ'.$lf;
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
            $filename = sprintf("%s.csv", $nendo."　".$testdivName1."会場ラベルデータ");
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
