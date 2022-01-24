<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd320aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjd320aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "OUTPUT_RANK"  =>  VARS::post("OUTPUT_RANK"),  //学年別、コース別順位
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "TESTCD"       =>  VARS::post("TESTCD"),       //テスト
                             "OUTPUT_JYOUKEN" =>  VARS::post("OUTPUT_JYOUKEN"), //出力条件(得点、順位)
                             "SCORE_RANGE"  =>  VARS::post("SCORE_RANGE"),  //条件　得点
                             "RANK_RANGE"   =>  VARS::post("RANK_RANGE"),   //条件　不振者順位
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata        = VARS::post("selectdata");
        if ($this->cmd === 'knjd320a') {
             $this->field["SCORE_RANGE"] = "";
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //出欠状況不振者
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        $header = array("GRADE_RANK"    => "順位",
                        "NAME"          => "氏名",
                        "HR_NAME"       => "クラス",
                        "ATTENDNO"      => "出席番号",
                        "SCORE"         => "得点",
                        "STAFFCD"       => "教科担当者"
                        );

        $contents = "";

        $wrk_nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);
        $grade = $db->getOne(knjd320aQuery::getSelectGrade($this, $this->field["GRADE"]));
        $testname = $db->getOne(knjd320aQuery::getTestcd($this, $this->field["TESTCD"]));
        $subclasscd = explode (",", $this->selectdata);
        $data = false;
        if ($subclasscd[0] !="") {
            for ($i = 0; $i < get_count($subclasscd); $i++){
                
                $subclassname = $db->getOne(knjd320aQuery::getSubclassMst($this, $subclasscd[$i]));

                $title = "得点不振者一覧";
                if ($subclasscd[$i] === '999999' && $subclassname == "") {
                    $contents .= $wrk_nendo ."　".$grade."　".$testname.$title."（総合計）\n";
                } else {
                    $contents .= $wrk_nendo ."　".$grade."　".$testname.$title."（".$subclassname."）\n";
                }
                
                //コース毎で順位指定の場合、テスト科目に含まれる該当コースを取得する
                if ($this->field["OUTPUT_RANK"] == "2" && $this->field["OUTPUT_JYOUKEN"] == "2") {
                    $query = knjd320aQuery::getCourseQuery($this, $subclasscd[$i]);
                    $result = $db->query($query);
                    $getcource = array();
                    $cource_count = 0;
                    while($cource = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $getcource[$cource_count]["COURSECD"] = $cource["COURSECD"];
                        $getcource[$cource_count]["MAJORCD"] = $cource["MAJORCD"];
                        $getcource[$cource_count]["COURSECODE"] = $cource["COURSECODE"];
                        $cource_count++;
                    }
                    $result->free();
                }
                //コース毎でかつ順位指定の場合
                if ($this->field["OUTPUT_RANK"] == "2" && $this->field["OUTPUT_JYOUKEN"] == "2" && $cource_count > 0) {
                    $query = knjd320aQuery::selectCsv2Query($this, $subclasscd[$i], $getcource, $cource_count);
                //それ以外の場合
                } else {
                    $query = knjd320aQuery::selectCsvQuery($this, $subclasscd[$i]);
                }
                $result = $db->query($query);
                $count = 0;
                $set_coursecode = "";
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    //ヘッダー付与
                    //学年別
                    if ($model->field["OUTPUT_RANK"] == "1") {
                        if ($count == 0) {
                            $contents .= implode($header, ",") ."\n";
                        }
                    //コース別
                    } else {
                        if ($count == 0) {
                            $set_coursecode = $row["COURSECODE"];
                            $contents .= $row["COURSECODENAME"] ."\n";
                            $contents .= implode($header, ",") ."\n";
                        } else {
                            //コースコードが異なる場合はコース名とヘッダーを新たに付与
                            if ($set_coursecode !== $row["COURSECODE"]) {
                                $set_coursecode = $row["COURSECODE"];
                                $contents .= $row["COURSECODENAME"] ."\n";
                                $contents .= implode($header, ",") ."\n";
                            }
                        }
                    }
                    //複数表示の場合は、SORTした最初の値のみをセット
                    if($count > 0) {
                        if($checkrow["SCHREGNO"] != $row["SCHREGNO"]) {
                            if ($subclasscd[$i] === '999999' && $subclassname == "") {
                                $contents .= $row["RANK"].",".$row["NAME"].",".$row["HR_NAME"].",".$row["ATTENDNO"].",".$row["SCORE"].",".'-'."\n";
                            } else {
                                $contents .= $row["RANK"].",".$row["NAME"].",".$row["HR_NAME"].",".$row["ATTENDNO"].",".$row["SCORE"].",".$row["STAFFNAME"]."\n";
                            }
                        }
                    } else {
                        if ($subclasscd[$i] === '999999' && $subclassname == "") {
                            $contents .= $row["RANK"].",".$row["NAME"].",".$row["HR_NAME"].",".$row["ATTENDNO"].",".$row["SCORE"].",".'-'."\n";
                        } else {
                            $contents .= $row["RANK"].",".$row["NAME"].",".$row["HR_NAME"].",".$row["ATTENDNO"].",".$row["SCORE"].",".$row["STAFFNAME"]."\n";
                        }
                    }
                    
                    $data = true;
                    $checkrow = array();
                    $checkrow["SCHREGNO"] = $row["SCHREGNO"];
                    $count++;
                }
                $result->free();
                if ($count > 0) {
                    $contents .= "\n\n";
                } else {
                    $contents .= '該当者なし'."\n\n";
                }
            }
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse = "得点不振者一覧";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
