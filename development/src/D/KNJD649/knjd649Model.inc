<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd649Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd649Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),         //データ区分
                             "MOCKCD"           =>  VARS::post("MOCKCD"),           //テスト名称
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),       //選択区分
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

	    $this->selectdata = VARS::post("selectdata");

    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents  = "";

        //明細表
        $selectdata = explode(",", $this->selectdata);
        $datacnt = get_count($selectdata);
        $data1 = false;
        for ($h = 0; $h < $datacnt; $h++) {
            //タイトル行
            $title = "学力テスト科目別得点分布表";
            $testname = $db->getOne(knjd649Query::getMockNameCsv($this));
            $contents .= ",".CTRL_YEAR ."年度,," .$title ." （ " .$testname ." ) \n\n";

            //選択項目名行
            $query = knjd649Query::getSelectNameCsv($this, $selectdata[$h]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= $row["NAME"]."\n";
            }

            //中高判定
            $Ikkankou = $db->getOne(knjd649Query::getIkkankou());
            $JH = ($Ikkankou != "" && $this->field["GRADE"] < '04') ? "J" : "H";

            //科目行
            $headcnt = 0;
            $contents .= "得点,";
            $mock_subclasscd = "";
            $query = knjd649Query::getSubclassNameCsv($this, $selectdata[$h]);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $std_cnt = $db->getRow(knjd649Query::getCntStudentCsv1($this, 0, 0, $row["MOCK_SUBCLASS_CD"], $selectdata[$h], "true"), DB_FETCHMODE_ASSOC);
                if($JH == 'H' && !$std_cnt["STUDENT3"]) continue;

                $contents .= ",".$row["SUBCLASS_NAME"].",,";
                $headcnt++;
                $mock_subclasscd .= ($mock_subclasscd == "") ? $row["MOCK_SUBCLASS_CD"] : ','.$row["MOCK_SUBCLASS_CD"];
            }
            $contents .= "\n,";

            //項目行
            for ($i = 0; $i < $headcnt; $i++) {
                $contents .= "男,女,合計,";
            }
            $contents .= "\n";

            //明細行
            $max_score = "100";
            $col = "20";
            $subclasscd = explode(',',$mock_subclasscd);

            for ($i = 0; $i <= $col; $i++) {
                $f_score = $max_score-($i*5);
                $t_score = ($i == 0) ? $f_score : $max_score-($i-1)*5-1;
                $contents .= $f_score.",";

                for ($j = 0; $j < $headcnt; $j++) {
                    $data_flg = false;
                    $query = knjd649Query::getCntStudentCsv1($this, $f_score, $t_score, $subclasscd[$j], $selectdata[$h]);
                    $result = $db->query($query);
                    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                        $row["STUDENT1"] = ($row["STUDENT1"]) ? $row["STUDENT1"] : "";
                        $row["STUDENT2"] = ($row["STUDENT2"]) ? $row["STUDENT2"] : "";
                        $row["STUDENT3"] = ($row["STUDENT3"]) ? $row["STUDENT3"] : "";

                        $contents .= $row["STUDENT1"].",".$row["STUDENT2"].",".$row["STUDENT3"].",";
                        $data_flg = true;
                    }
                    if(!$data_flg) $contents .= ",,,";
                }
                $contents .= "\n";
            }

            //受験者数出力行
            $contents .= "受験者数,";
            $total = true;
            for ($j = 0; $j < $headcnt; $j++) {
                $data_flg = false;
                $query = knjd649Query::getCntStudentCsv1($this, $f_score, $t_score, $subclasscd[$j], $selectdata[$h], $total);
                $result = $db->query($query);
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $row["STUDENT1"] = ($row["STUDENT1"]) ? $row["STUDENT1"] : "";
                    $row["STUDENT2"] = ($row["STUDENT2"]) ? $row["STUDENT2"] : "";
                    $row["STUDENT3"] = ($row["STUDENT3"]) ? $row["STUDENT3"] : "";

                    $contents .= $row["STUDENT1"].",".$row["STUDENT2"].",".$row["STUDENT3"].",";
                    $data_flg = true;
                    $data1 = true;
                }
                if(!$data_flg) $contents .= ",,,";
            }
            $contents .= "\n\n\n";
        }

        //集計表
        $data2 = false;
        $data3 = false;
        for ($h = 1; $h <= 2; $h++) {

            //データがない場合、出力しない
            $gr_hr = explode(",", $this->selectdata);
            $mock_subclasscd = ($h == "1") ? '333333' : '555555';
            $total_std_cnt = 0;
            foreach($gr_hr as $key){
                $std_cnt = $db->getRow(knjd649Query::getCntStudentCsv2($this, $f_score, $t_score, $mock_subclasscd, $key, "true"), DB_FETCHMODE_ASSOC);
                $total_std_cnt += $std_cnt["STUDENT"];
            }
            if($total_std_cnt == 0) continue;

            $total = false;
            //タイトル行
            $title = "学力テスト得点分布表";
            $subtitle = ($h == "1") ? "3教科" : "5教科";
            $contents .= ",".CTRL_YEAR."年度,,".$title." （ " .$testname ." ) \n";
            $contents .= ",,,,（ ".$subtitle." ) \n\n";

            //選択項目名称行
            $query = knjd649Query::getItemNameCsv($this);
            $contents .= "得点,";
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= $row["NAME"].",";
            }
            $contents .= "\n";

            //明細行
            $max_score = ($h == "1") ? "300" : "500";
            $col = ($h == "1") ? "30" : "50";
            $subclasscd = ($h == "1") ? '333333' : '555555';

            for ($i = 0; $i <= $col; $i++) {

                $f_score = $max_score-($i*10);
                $t_score = ($i == 0) ? $f_score : $max_score-($i-1)*10-1;
                $contents .= $f_score.",";

                $selectdata = explode(",", $this->selectdata);
                $datacnt = get_count($selectdata);

                for ($j = 0; $j < $datacnt; $j++) {
                    $data_flg = false;
                    $query = knjd649Query::getCntStudentCsv2($this, $f_score, $t_score, $subclasscd, $selectdata[$j]);
                    $result = $db->query($query);
                    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                        $row["STUDENT"] = ($row["STUDENT"]) ? $row["STUDENT"] : "";

                        $contents .= $row["STUDENT"].",";
                        $data_flg = true;
                    }
                    if(!$data_flg) $contents .= ",";
                }
                $contents .= "\n";
            }

            //受験者数出力行
            $contents .= "受験者数,";
            $total = true;

            $selectdata = explode(",", $this->selectdata);
            $datacnt = get_count($selectdata);

            for ($j = 0; $j < $datacnt; $j++) {
                $data_flg = false;
                $query = knjd649Query::getCntStudentCsv2($this, $f_score, $t_score, $subclasscd, $selectdata[$j], $total);
                $result = $db->query($query);
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $row["STUDENT"] = ($row["STUDENT"]) ? $row["STUDENT"] : "";

                    $contents .= $row["STUDENT"].",";
                    $data_flg = true;

                    if($h == "1"){
                        $data2 = true;
                    } else {
                        $data3 = true;
                    }
                }
                if(!$data_flg) $contents .= ",";
            }
            $contents .= "\n\n\n";
        }

        Query::dbCheckIn($db);

        if ($data1 || $data2 || $data3){
	        $couse = "学力テスト得点分布表";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
