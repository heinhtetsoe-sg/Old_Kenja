<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl316rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;
        
    /**
     * メンバ変数の初期化
     */
    function knjl316rModel()
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
                             "COURSE_MAJORCD" =>  VARS::post("COURSE_MAJORCD") //課程学科
                     );
                     

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }

        $this->cmd         = VARS::request("cmd");
    }
    
    //CSV出力処理
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        
        //ヘッダー情報取得
        //入試制度
        $applicantdivName = $db->getOne(knjl316rQuery::getNameMst($this, "L003", "NAME"));
        //入試区分
        $testdivName = $db->getOne(knjl316rQuery::getNameMst($this, "L004", "NAME"));
        //課程学科名
        if ($this->field["COURSE_MAJORCD"] !== '9999') {
            $courseMajorName = $db->getOne(knjl316rQuery::getCourseMajorcd($this, "NAME"));
        } else {
            $courseMajorName = '全て';
        }
        //ヘッダ配列
        $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $time = "　".date('H時i分現在');
        $header = array("入試年度：".$this->ObjYear,
                        "入試制度：".$applicantdivName,
                        "入試区分：".$testdivName,
                        "課程学科：".$courseMajorName,
                        $date.$time,
                        );
        //サブヘッダ配列
        $subheader = array("内申点(75)",
                           "平均点",
                           "人数",
                           );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;
        $subheaderContents = implode($subheader, $comma) .$lf;
        
        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        $query = knjl316rQuery::getCsvQuery($this);
        if ('' != $query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //総合点の平均点
                $avarage = round($row["TOTAL4_SUM"] / $row["CNT"], 1);
                
                $dataContents .= $row["NAISIN2"] . ",";
                $dataContents .= $avarage. ",";
                $dataContents .= $row["CNT"];
                $dataContents .= $lf;
                
                $data = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $subheaderContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "得点･内甲点75相関データ");
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
