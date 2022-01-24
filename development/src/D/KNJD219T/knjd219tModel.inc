<?php

require_once('for_php7.php');

//定数定義
class knjd219tModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $subclasscd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    //メンバ変数の初期化
    function knjd219tModel()
    {
    }
    function init()
    {
        //評定値の段階
        $this->setAssesslevelCount = "5";
        
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->subclasscd = "";
            $this->grade = "";
            $this->semester = "";
            $this->testkind_itemcd = "";
            $this->ruiseki_div     = "";
            $this->div             = "";
            $this->hr_class        = "";
            $this->coursecd        = "";
            $this->groupcd         = "";
            $this->coursecode      = "";
            $this->field = array();
            $this->field2 = array();
            common::GetControlMaster_Fnc($this->control);
        }

        //左画面コンボ変更時
        if ($this->cmd === 'knjd219t') {
            $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                                 "RUISEKI_DIV"      =>  VARS::post("RUISEKI_DIV"),      //累積区分
                                 "SET_DIV"          =>  VARS::post("SET_DIV"),          //区分
                                 "GRADE"            =>  VARS::post("GRADE"),            //学年
                                 "GROUP_CD"         =>  VARS::post("GROUP_CD"),         //グループコード
                                 "COURSE_MAJOR_CODE"=>  VARS::post("COURSE_MAJOR_CODE"),//コース
                                 "TESTKIND_ITEMCD"  =>  VARS::post("TESTKIND_ITEMCD"),  //テスト種別
                                 "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                                );
        }
        //右画面コンボ変更時
        $this->subclasscd = VARS::post("SUBCLASSCD");   //科目
        
        //右画面の各項目
        for ($i = 1; $i <= $this->setAssesslevelCount; $i++) { 
            //下限値
            $assesslow  = "ASSESSLOW_".$i;
            $this->field2["ASSESSLOW_".$i]  = VARS::post($assesslow);
            //上限値
            $assesshigh  = "ASSESSHIGH_".$i;
            $this->field2["ASSESSHIGH_".$i]  = VARS::post($assesshigh);
        }
        //評定1の上限値は評定2の下限値 - 1をセット
        $this->field2["ASSESSHIGH_1"]  = $this->field2["ASSESSLOW_2"] - 1;
        
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd             = VARS::request("cmd");
        
        //リンク用から取得した科目
        if (VARS::get("SUBCLASS_LINK")!="") {
            $this->semester         = VARS::get("SEND_SEMESTER");
            $this->testkind_itemcd  = VARS::get("SEND_TESTKIND_ITEMCD");
            $this->subclasscd       = VARS::get("SEND_SUBCLASSCD");
            $this->ruiseki_div      = VARS::get("SEND_RUISEKI_DIV");
            $this->div              = VARS::get("SEND_DIV");
            $this->grade            = VARS::get("SEND_GRADE");
            $this->hr_class         = VARS::get("SEND_HR_CLASS");
            $this->coursecd         = VARS::get("SEND_COURSECD");
            $this->groupcd          = VARS::get("SEND_MAJORCD");
            $this->coursecode       = VARS::get("SEND_COURSECODE");
        }
        //プロパティファイル
        $this->getPropertiesAll();
    }
    
    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->grade == "") {
            $this->setWarning("MSG304", "( 学年 )");
            return false;
        }
        if ($this->schoolkind === 'H') {
            if ($this->groupcd == "") {
                $this->setWarning("MSG304", "( コースグループ )");
                return false;
            }
        } else if ($this->schoolkind === 'J') {
            if ($this->coursecd == "" && $this->groupcd == "" &&  $this->coursecode == "") {
                $this->setWarning("MSG304", "( コース )");
                return false;
            }
        }
        if ($this->testkind_itemcd == "") {
            $this->setWarning("MSG304", "( テスト種別 )");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG304", "( 科目 )");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjd219tQuery::delete($this, $db);
            if ($this->cmd === 'update') {
                knjd219tQuery::update($this, $db);
            }
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
        return true;
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        $contents = "";
        //ヘッダ配列
        $header = array("学年",
                        "考査別又は累積別",
                        "テスト種別",
                        "コースグループ",
                        "科目コード",
                        "",
                        "",
                        "",
                        "科目名",
                        "評定5",
                        "",
                        "評定4",
                        "",
                        "評定3",
                        "",
                        "評定2",
                        "",
                        "評定1",
                        ""
                        );
        //ヘッダ配列を出力変数にセット
        $contents .= implode($header, $comma) .$lf;

        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjd219tQuery::selectQuery($this, "csv");
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["GRADE_NAME1"] .$comma;
            $contents .= $row["RUISEKI_DIV_NAME"] .$comma;
            $contents .= $row["TESTITEMNAME"] .$comma;
            $contents .= $row["SET_NAME"] .$comma;
            $contents .= $row["CLASSCD"] .$comma;
            $contents .= $row["SCHOOL_KIND"] .$comma;
            $contents .= $row["CURRICULUM_CD"] .$comma;
            $contents .= $row["SUBCLASSCD"] .$comma;
            $contents .= $row["SUBCLASSNAME"] .$comma;
            $contents .= $row["ASSESSLOW_5"] .$comma;
            $contents .= $row["ASSESSHIGH_5"] .$comma;
            $contents .= $row["ASSESSLOW_4"] .$comma;
            $contents .= $row["ASSESSHIGH_4"] .$comma;
            $contents .= $row["ASSESSLOW_3"] .$comma;
            $contents .= $row["ASSESSHIGH_3"] .$comma;
            $contents .= $row["ASSESSLOW_2"] .$comma;
            $contents .= $row["ASSESSHIGH_2"] .$comma;
            $contents .= $row["ASSESSLOW_1"] .$comma;
            $contents .= $row["ASSESSHIGH_1"];
            $contents .= $lf;

            $header_data = true;
        }
        $result->free();
        //学期名
        if ($header_data) {
            $semesterName = $db->getOne(knjd219tQuery::getSemester($this->field["SEMESTER"]));
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $setName = CTRL_YEAR."年度_".$semesterName."_ライン点基準の評定一覧";
            $filename = sprintf("%s.csv",$setName);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
