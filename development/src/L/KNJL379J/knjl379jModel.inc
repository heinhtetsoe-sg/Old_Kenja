<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl379jModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl379jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("DBNAME"   =>  VARS::post("DBNAME"));      //DB変更に備えて

        $this->cmd         = VARS::request("cmd");

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

	/**
	 * 
	 */
    function getDownloadModel()
    {
		//ヘッダの作成//
        $header1 = array("APPLICANTDIV"  => "",
                         "EXAMNO"        => "",
                         "NAME"          => "",
                         "SEX"           => "",
                         "SCORE11"       => "Ａ－１",
                         "SCORE12"       => "",
                         "SCORE13"       => "",
                         "SCORE14"       => "",
                         "STD_SCORE11"   => "",
                         "STD_SCORE12"   => "",
                         "STD_SCORE13"   => "",
                         "STD_SCORE14"   => "",
                         "JUDGE_DIV1"    => "",
                         "EXAM_TYPE1"    => "",
                         "SCORE21"       => "Ａ－２",
                         "SCORE22"       => "",
                         "SCORE23"       => "",
                         "SCORE24"       => "",
                         "STD_SCORE21"   => "",
                         "STD_SCORE22"   => "",
                         "STD_SCORE23"   => "",
                         "STD_SCORE24"   => "",
                         "JUDGE_DIV2"    => "",
                         "EXAM_TYPE2"    => "",
                         "SCORE31"       => "Ｂ",
                         "SCORE32"       => "",
                         "SCORE33"       => "",
                         "SCORE34"       => "",
                         "STD_SCORE31"   => "",
                         "STD_SCORE32"   => "",
                         "STD_SCORE33"   => "",
                         "STD_SCORE34"   => "",
                         "JUDGE_DIV3"    => "",
                         "EXAM_TYPE3"    => "",
                         "SCORE41"       => "Ｃ",
                         "SCORE42"       => "",
                         "SCORE43"       => "",
                         "SCORE44"       => "",
                         "STD_SCORE41"   => "",
                         "STD_SCORE42"   => "",
                         "STD_SCORE43"   => "",
                         "STD_SCORE44"   => "",
                         "JUDGE_DIV4"    => "",
                         "EXAM_TYPE4"    => "",
                         "SCORE61"       => "Ｄ",
                         "SCORE62"       => "",
                         "SCORE63"       => "",
                         "SCORE64"       => "",
                         "STD_SCORE61"   => "",
                         "STD_SCORE62"   => "",
                         "STD_SCORE63"   => "",
                         "STD_SCORE64"   => "",
                         "JUDGE_DIV6"    => "",
                         "EXAM_TYPE6"    => "",
                         "SCORE51"       => "帰国生",
                         "SCORE52"       => "",
                         "SCORE53"       => "",
                         "STD_SCORE51"   => "",
                         "STD_SCORE52"   => "",
                         "STD_SCORE53"   => "",
                         "JUDGE_DIV5"    => "",
                         "EXAM_TYPE5"    => "",
                         "ENTDIV"        => ""
                        );
        $header2 = array("APPLICANTDIV"  => "入試制度",
                         "EXAMNO"        => "受験番号",
                         "NAME"          => "氏名",
                         "SEX"           => "性別",
                         "SCORE11"       => "得点",
                         "SCORE12"       => "",
                         "SCORE13"       => "",
                         "SCORE14"       => "",
                         "STD_SCORE11"   => "偏差値",
                         "STD_SCORE12"   => "",
                         "STD_SCORE13"   => "",
                         "STD_SCORE14"   => "",
                         "JUDGE_DIV1"    => "合否区分",
                         "EXAM_TYPE1"    => "受験科目",
                         "SCORE21"       => "得点",
                         "SCORE22"       => "",
                         "SCORE23"       => "",
                         "SCORE24"       => "",
                         "STD_SCORE21"   => "偏差値",
                         "STD_SCORE22"   => "",
                         "STD_SCORE23"   => "",
                         "STD_SCORE24"   => "",
                         "JUDGE_DIV2"    => "合否区分",
                         "EXAM_TYPE2"    => "受験科目",
                         "SCORE31"       => "得点",
                         "SCORE32"       => "",
                         "SCORE33"       => "",
                         "SCORE34"       => "",
                         "STD_SCORE31"   => "偏差値",
                         "STD_SCORE32"   => "",
                         "STD_SCORE33"   => "",
                         "STD_SCORE34"   => "",
                         "JUDGE_DIV3"    => "合否区分",
                         "EXAM_TYPE3"    => "受験科目",
                         "SCORE41"       => "得点",
                         "SCORE42"       => "",
                         "SCORE43"       => "",
                         "SCORE44"       => "",
                         "STD_SCORE41"   => "偏差値",
                         "STD_SCORE42"   => "",
                         "STD_SCORE43"   => "",
                         "STD_SCORE44"   => "",
                         "JUDGE_DIV4"    => "合否区分",
                         "EXAM_TYPE4"    => "受験科目",
                         "SCORE61"       => "得点",
                         "SCORE62"       => "",
                         "SCORE63"       => "",
                         "SCORE64"       => "",
                         "STD_SCORE61"   => "偏差値",
                         "STD_SCORE62"   => "",
                         "STD_SCORE63"   => "",
                         "STD_SCORE64"   => "",
                         "JUDGE_DIV6"    => "合否区分",
                         "EXAM_TYPE6"    => "受験科目",
                         "SCORE51"       => "得点",
                         "SCORE52"       => "",
                         "SCORE53"       => "",
                         "STD_SCORE51"   => "偏差値",
                         "STD_SCORE52"   => "",
                         "STD_SCORE53"   => "",
                         "JUDGE_DIV5"    => "合否区分",
                         "EXAM_TYPE5"    => "受験科目",
                         "ENTDIV"        => "入学状況"
                        );
        $header3 = array("APPLICANTDIV"  => "",
                         "EXAMNO"        => "",
                         "NAME"          => "",
                         "SEX"           => "",
                         "SCORE11"       => "国語",
                         "SCORE12"       => "算数",
                         "SCORE13"       => "理科",
                         "SCORE14"       => "社会",
                         "STD_SCORE11"   => "国語",
                         "STD_SCORE12"   => "算数",
                         "STD_SCORE13"   => "理科",
                         "STD_SCORE14"   => "社会",
                         "JUDGE_DIV1"    => "",
                         "EXAM_TYPE1"    => "",
                         "SCORE21"       => "国語",
                         "SCORE22"       => "算数",
                         "SCORE23"       => "理科",
                         "SCORE24"       => "社会",
                         "STD_SCORE21"   => "国語",
                         "STD_SCORE22"   => "算数",
                         "STD_SCORE23"   => "理科",
                         "STD_SCORE24"   => "社会",
                         "JUDGE_DIV2"    => "",
                         "EXAM_TYPE2"    => "",
                         "SCORE31"       => "国語",
                         "SCORE32"       => "算数",
                         "SCORE33"       => "理科",
                         "SCORE34"       => "社会",
                         "STD_SCORE31"   => "国語",
                         "STD_SCORE32"   => "算数",
                         "STD_SCORE33"   => "理科",
                         "STD_SCORE34"   => "社会",
                         "JUDGE_DIV3"    => "",
                         "EXAM_TYPE3"    => "",
                         "SCORE41"       => "国語",
                         "SCORE42"       => "算数",
                         "SCORE43"       => "理科",
                         "SCORE44"       => "社会",
                         "STD_SCORE41"   => "国語",
                         "STD_SCORE42"   => "算数",
                         "STD_SCORE43"   => "理科",
                         "STD_SCORE44"   => "社会",
                         "JUDGE_DIV4"    => "",
                         "EXAM_TYPE4"    => "",
                         "SCORE61"       => "国語",
                         "SCORE62"       => "算数",
                         "SCORE63"       => "理科",
                         "SCORE64"       => "社会",
                         "STD_SCORE61"   => "国語",
                         "STD_SCORE62"   => "算数",
                         "STD_SCORE63"   => "理科",
                         "STD_SCORE64"   => "社会",
                         "JUDGE_DIV6"    => "",
                         "EXAM_TYPE6"    => "",
                         "SCORE51"       => "国語",
                         "SCORE52"       => "算数",
                         "SCORE53"       => "英語",
                         "STD_SCORE51"   => "国語",
                         "STD_SCORE52"   => "算数",
                         "STD_SCORE53"   => "英語",
                         "JUDGE_DIV5"    => "",
                         "EXAM_TYPE5"    => "",
                         "ENTDIV"        => ""
                        );

        //DB接続
        $db = Query::dbCheckOut();

        $contents  = $this->ObjYear."年度　得点･偏差値一覧,\n";
        $contents .= implode($header1, ",") ."\n";
        $contents .= implode($header2, ",") ."\n";
        $contents .= implode($header3, ",") ."\n";

        //入試制度名称取得
        $query1 = knjl379jQuery::getSelectName("L003");
        $result1 = $db->query($query1);
		$opt_name1 = array();
        while( $row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name1[$row1["NAMECD2"]] = $row1["NAMECD2"] . '：' .$row1["NAME1"];
        }

        //受験型名称取得
        $query2 = knjl379jQuery::getSelectName("L005");
        $result2 = $db->query($query2);
		$opt_name2 = array();
        while( $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name2[$row2["NAMECD2"]] = $row2["NAME1"];
        }

        //SQL文発行
        $query = knjl379jQuery::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            for ($i = 1; $i < 7; $i++) {

                if($row["EXAMINEE_DIV".$i] == '1' && $row["JUDGEDIV".$i] == '1'){
                    $row["JUDGE_DIV".$i] = '◎';
                } elseif($row["JUDGEDIV".$i] == '2' && $row["SPECIAL_MEASURES"] == '1'){
                    $row["JUDGE_DIV".$i] = '◇';
                } elseif($row["EXAMINEE_DIV".$i] == '1' && $row["JUDGEDIV".$i] == '2'){
                    $row["JUDGE_DIV".$i] = '×';
                } elseif($row["EXAMINEE_DIV".$i] == '2'){
                    $row["JUDGE_DIV".$i] = '－';
                } else {
                    $row["JUDGE_DIV".$i] = '';
                }

                $row["EXAM_TYPE".$i] = $opt_name2[$row["EXAM_TYPE".$i]];

            }

            $row["APPLICANTDIV"] = $opt_name1[$row["APPLICANTDIV"]];

            if($row["SEX"] == '1'){
                $row["SEX"] = '男';
            } elseif($row["SEX"] == '2'){
                $row["SEX"] = '女';
            } else {
                $row["SEX"] = '';
            }

            $row["ENTDIV"] = ($row["ENTDIV"] == '1') ? '入学' : '';

            $temp = array("APPLICANTDIV"  => $row["APPLICANTDIV"],
                          "EXAMNO"        => $row["EXAMNO"],
                          "NAME"          => $row["NAME"],
                          "SEX"           => $row["SEX"],
                          "SCORE11"       => $row["SCORE11"],
                          "SCORE12"       => $row["SCORE12"],
                          "SCORE13"       => $row["SCORE13"],
                          "SCORE14"       => $row["SCORE14"],
                          "STD_SCORE11"   => $row["STD_SCORE11"],
                          "STD_SCORE12"   => $row["STD_SCORE12"],
                          "STD_SCORE13"   => $row["STD_SCORE13"],
                          "STD_SCORE14"   => $row["STD_SCORE14"],
                          "JUDGE_DIV1"    => $row["JUDGE_DIV1"],
                          "EXAM_TYPE1"    => $row["EXAM_TYPE1"],
                          "SCORE21"       => $row["SCORE21"],
                          "SCORE22"       => $row["SCORE22"],
                          "SCORE23"       => $row["SCORE23"],
                          "SCORE24"       => $row["SCORE24"],
                          "STD_SCORE21"   => $row["STD_SCORE21"],
                          "STD_SCORE22"   => $row["STD_SCORE22"],
                          "STD_SCORE23"   => $row["STD_SCORE23"],
                          "STD_SCORE24"   => $row["STD_SCORE24"],
                          "JUDGE_DIV2"    => $row["JUDGE_DIV2"],
                          "EXAM_TYPE2"    => $row["EXAM_TYPE2"],
                          "SCORE31"       => $row["SCORE31"],
                          "SCORE32"       => $row["SCORE32"],
                          "SCORE33"       => $row["SCORE33"],
                          "SCORE34"       => $row["SCORE34"],
                          "STD_SCORE31"   => $row["STD_SCORE31"],
                          "STD_SCORE32"   => $row["STD_SCORE32"],
                          "STD_SCORE33"   => $row["STD_SCORE33"],
                          "STD_SCORE34"   => $row["STD_SCORE34"],
                          "JUDGE_DIV3"    => $row["JUDGE_DIV3"],
                          "EXAM_TYPE3"    => $row["EXAM_TYPE3"],
                          "SCORE41"       => $row["SCORE41"],
                          "SCORE42"       => $row["SCORE42"],
                          "SCORE43"       => $row["SCORE43"],
                          "SCORE44"       => $row["SCORE44"],
                          "STD_SCORE41"   => $row["STD_SCORE41"],
                          "STD_SCORE42"   => $row["STD_SCORE42"],
                          "STD_SCORE43"   => $row["STD_SCORE43"],
                          "STD_SCORE44"   => $row["STD_SCORE44"],
                          "JUDGE_DIV4"    => $row["JUDGE_DIV4"],
                          "EXAM_TYPE4"    => $row["EXAM_TYPE4"],
                          "SCORE61"       => $row["SCORE61"],
                          "SCORE62"       => $row["SCORE62"],
                          "SCORE63"       => $row["SCORE63"],
                          "SCORE64"       => $row["SCORE64"],
                          "STD_SCORE61"   => $row["STD_SCORE61"],
                          "STD_SCORE62"   => $row["STD_SCORE62"],
                          "STD_SCORE63"   => $row["STD_SCORE63"],
                          "STD_SCORE64"   => $row["STD_SCORE64"],
                          "JUDGE_DIV6"    => $row["JUDGE_DIV6"],
                          "EXAM_TYPE6"    => $row["EXAM_TYPE6"],
                          "SCORE51"       => $row["SCORE51"],
                          "SCORE52"       => $row["SCORE52"],
                          "SCORE53"       => $row["SCORE53"],
                          "STD_SCORE51"   => $row["STD_SCORE51"],
                          "STD_SCORE52"   => $row["STD_SCORE52"],
                          "STD_SCORE53"   => $row["STD_SCORE53"],
                          "JUDGE_DIV5"    => $row["JUDGE_DIV5"],
                          "EXAM_TYPE5"    => $row["EXAM_TYPE5"],
                          "ENTDIV"        => $row["ENTDIV"]
                         );

	        $contents .= implode($temp, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
	        $couse="得点・偏差値一覧";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
