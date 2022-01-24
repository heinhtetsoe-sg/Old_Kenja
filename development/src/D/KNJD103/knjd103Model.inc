<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd103Model extends Model {
    var $field      = array();
    var $select_data      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd103Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"			    =>  VARS::post("YEAR"),		        //年度
							 "GAKKI"			=>	VARS::post("GAKKI"),	        //学期コード
							 "TESTKINDCD"       =>	VARS::post("TESTKINDCD"),       //テスト名/* NO001 */
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

	    $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
    }

    function getDownloadModel()
    {
        $db = Query::dbCheckOut();
        $contents = "";

		//タイトル
        $wareki_year = common::DateConv1($this->control["学籍処理日"],10);	//年度
        $semester_name = $this->control["学期名"][$this->field["GAKKI"]];  //学期名
        $test_name = ($this->field["TESTKINDCD"] == "01") ? "中間テスト" : "期末テスト" ;  //テスト名
        $title = $wareki_year."　".$semester_name."　".$test_name."　個人成績票";
        $contents = $title . "\n";

		//タイトル
        $tanninn = "学級担任：" . $db->getOne(knjd103Query::getStaffName($this));
        $sakuseibi = "作成日：" . common::DateConv1($this->control["学籍処理日"],0);
        $contents .= $tanninn .",,,". $sakuseibi . "\n";

		//ヘッダの作成
        $header[] = "年組番";
        $header[] = "氏名";
        $header[] = "科目";

		$subclasscd = array();
   	    $query = knjd103Query::getSubclassCsvData($this);//科目一覧を取得
       	$result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	    	$subclasscd[]   = $row["SUBCLASSCD"];
	    	$header[]       = $row["SUBCLASSABBV"];
       	}
        $result->free();

        $header[] = "合計";
        $header[] = "平均";
        $contents .= implode(",",$header) . "\n";

        $shubet = array("成績","学級平均点","学級順位","学年平均点","学年順位");

        $koumoku = array();
        $koumoku_kei = array();
        $koumoku_avg = array();
        $schno = "0";
        $subclassno = "0";
        $data = false;
   	    $query = knjd103Query::getCsvData($this);//ＣＳＶ出力
       	$result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //生徒のブレイク時
            if ($schno != "0" && $schno != $row["SCHREGNO"]) {
                for ($i=0; $i<get_count($shubet); $i++) {
        	    	$contents .= implode(",",$koumoku) . ",";
        	    	$contents .= $shubet[$i] . ",";
                    for ($j=0; $j<get_count($subclasscd); $j++) 
                        $contents .= $kamoku[$i][$j] . ",";
        	    	$contents .= $koumoku_kei[$i] . ",";
        	    	$contents .= $koumoku_avg[$i] . "\n";

                    for ($j=0; $j<get_count($subclasscd); $j++) $kamoku[$i][$j] = "";//クリア
                }
            }
            $koumoku[0] = $row["HR_NAME"].$row["ATTENDNO"]."番";//年組番
            $koumoku[1] = $row["NAME"];//氏名
            for ($i=0; $i<get_count($shubet); $i++) {
                for ($j=0; $j<get_count($subclasscd); $j++) 
                    if ($subclasscd[$j] == $row["SUBCLASSCD"]) $kamoku[$i][$j] = $row["SCORE".($i+1)];//科目
                $koumoku_kei[$i] = $row["SCORE".($i+1)."_SUM"];//合計
                $koumoku_avg[$i] = $row["SCORE".($i+1)."_AVG"];//平均
            }

            $schno = $row["SCHREGNO"];
            $subclassno = $row["SUBCLASSCD"];
            $data = true;
       	}
        if ($data) {
            for ($i=0; $i<get_count($shubet); $i++) {
      	    	$contents .= implode(",",$koumoku) . ",";
       	    	$contents .= $shubet[$i] . ",";
                for ($j=0; $j<get_count($subclasscd); $j++) 
                    $contents .= $kamoku[$i][$j] . ",";
       	    	$contents .= $koumoku_kei[$i] . ",";
       	    	$contents .= $koumoku_avg[$i] . "\n";
            }
        }
        $result->free();

        //DB切断
   	    Query::dbCheckIn($db);

        if ($data){
	        $couse="個人成績票";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り


    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
