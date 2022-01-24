<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm060Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjm060Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),         //年度
                             "SUBCLASSNAME"         =>  VARS::post("SUBCLASSNAME"), //科目
                             "GAKKI"                =>  VARS::post("GAKKI"),        //学期
                             "OUTDIV"               =>  VARS::post("OUTDIV"),       //出力区分
                             "HEADER"               =>  VARS::post("HEADER"),       //ヘッダ出力
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
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
        switch ($this->field["GAKKI"]){
            case "0";
                if ($this->field["OUTDIV"] == 0){
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM2_TERM_SCORE"   => "後期素点",
                                        "SEM1_VALUE"        => "前期評価",
                                        "SEM2_VALUE"        => "後期評価",
                                        "GRAD_VALUE"        => "学年評定"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM2_TERM_SCORE"   => "後期素点",
                                        "SEM1_VALUE"        => "前期評価",
                                        "SEM2_VALUE"        => "後期評価",
                                        "GRAD_VALUE"        => "学年評定"
                                        );
                    }
                }else if ($this->field["OUTDIV"] == 1) {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM2_TERM_SCORE"   => "後期素点"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM2_TERM_SCORE"   => "後期素点"
                                        );
                    }
                }else {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_VALUE"        => "前期評価",
                                        "SEM2_VALUE"        => "後期評価",
                                        "GRAD_VALUE"        => "学年評定"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_VALUE"        => "前期評価",
                                        "SEM2_VALUE"        => "後期評価",
                                        "GRAD_VALUE"        => "学年評定"
                                        );
                    }
                }
                break ;
            case "1";
                if ($this->field["OUTDIV"] == 0){
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM1_VALUE"        => "前期評価"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点",
                                        "SEM1_VALUE"        => "前期評価"
                                        );
                    }
                }else if ($this->field["OUTDIV"] == 1) {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_TERM_SCORE"   => "前期素点"
                                        );
                    }
                }else {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_VALUE"        => "前期評価"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM1_VALUE"        => "前期評価"
                                        );
                    }
                }
                break ;
            case "2";
                if ($this->field["OUTDIV"] == 0){
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_TERM_SCORE"   => "後期素点",
                                        "SEM2_VALUE"        => "後期評価"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_TERM_SCORE"   => "後期素点",
                                        "SEM2_VALUE"        => "後期評価"
                                        );
                    }
                }else if ($this->field["OUTDIV"] == 1) {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_TERM_SCORE"   => "後期素点"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_TERM_SCORE"   => "後期素点"
                                        );
                    }
                }else {
                    if ($this->Properties["useCurriculumcd"] == "1") {
                        $header = array("CLASSCD"           => "教科コード",
                                        "SCHOOL_KIND"       => "学校校種",
                                        "CURRICULUM_CD"     => "課程コード",
                                        "SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_VALUE"        => "後期評価"
                                        );
                    } else {
                        $header = array("SUBCLASSCD"        => "科目コード",
                                        "SUBCLASSNAME"      => "科目名",
                                        "SCHREGNO"          => "学籍番号",
                                        "NAME"              => "氏名",
                                        "SEM2_VALUE"        => "後期評価"
                                        );
                    }
                }
                break ;
            default;
                $this->setWarning("学期が不正です。" .$this->field["GAKKI"]);
                return false;
        }

        $contents = "" ;
        if ($this->field["HEADER"] == "on"){
            $contents .= implode($header, ",") ."\n";
        }
        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        $query = knjm060Query::selectCsvQuery($this);

        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "個人別科目別成績一覧表";
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
