<?php

require_once('for_php7.php');

class knjh569Model extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;
    var $control;

    function knjh569Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("PROFICIENCYDIV"           =>  VARS::post("PROFICIENCYDIV"),           //データ種別
                             "PROFICIENCYCD"            =>  VARS::post("PROFICIENCYCD"),            //テスト名称
                             "SELECT_DIV"               =>  VARS::post("SELECT_DIV"),               //順位区分
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年
                             "PROFICIENCY_SUBCLASS_CD"  =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),  //実力科目
                             "PRINT_COUNT"              =>  VARS::post("PRINT_COUNT"),              //上位出力人数
                             "RANK_DATA_DIV"            =>  VARS::post("RANK_DATA_DIV"),            //基準点
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //校種
        $this->schoolkind = $db->getOne(knjh569Query::getSchoolKind($this));

        //ヘッダデータ
        $header = array();
        $header["SUBCLASS_NAME"]    = "科目名";
        $header["RANK"]             = "順位";
        $header["HR_NAME"]          = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["SCORE"]            = "得点";
        if ($this->schoolkind == 'H') {
            $header["DEVIATION"]        = "偏差値";
        }

        $contents  = "";    //初期化
        $dataFlg = false;   //出力フラグ

        //学期名
        $semesterName = $db->getOne(knjh569Query::getSemester($this->field["SEMESTER"]));
        //学年名
        $gradeName = $db->getOne(knjh569Query::getGrade($this, $this->field["SEMESTER"], $this->field["GRADE"]));
        //テスト名
        $proficiencycdName = $db->getOne(knjh569Query::getProName($this, $this->field["PROFICIENCYCD"]));
        //実力科目名
        $subclasscdName = (strlen($this->field["PROFICIENCY_SUBCLASS_CD"]) > 0) ? $db->getOne(knjh569Query::getProfSubclassMst($this, $this->field["PROFICIENCY_SUBCLASS_CD"])) : '全科目';
        //順位名
        $select_div = array(1 => '学年順位', 2 => 'クラス順位', 3 => 'コース順位');

        //1行目（タイトル等）
        $contents .= CTRL_YEAR.'年度 '.$semesterName.' '.$gradeName.' '.$proficiencycdName.'('.$subclasscdName.' '.$select_div[$this->field["SELECT_DIV"]].')'."\n";

        //2行目（ヘッダ）
        $contents .= implode($header, ",")."\n";

        //データ出力
        $query = knjh569Query::selectMainQuery($this);
        $result = $db->query($query);
        $tmpKey = $tmpScore = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($tmpKey != $row["KEY"]) {
                $tmpKey = $row["KEY"];
                $counter = 0;
                $tmpScore = "";
            }

            $counter++;
            $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($tmpScore && $tmpScore > $row["SCORE"]) continue;

            $sep = '';
            foreach ($header as $key => $label) {
                $contents .= $sep.$row[$key];
                $sep = ',';
            }
            $contents .= "\n";

            if ($counter == $this->field["PRINT_COUNT"]) $tmpScore = $row["SCORE"];
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($dataFlg) {
            $couse = $gradeName."実力テスト成績優秀者一覧";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
