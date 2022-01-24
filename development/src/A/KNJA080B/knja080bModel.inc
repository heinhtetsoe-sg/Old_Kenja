<?php

require_once('for_php7.php');

class knja080bModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $control;
    var $min_semester;  //最小学期
    var $schregno;      //学籍番号（左リスト）
    var $schoolName;
    var $schoolDiv;
    
    function knja080bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {           
        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::request("selectdata");
        if (VARS::get("PROGRAMID")){
            unset($this->newclass);
            unset($this->oldclass);
        }
        if (VARS::get("NEWCLASS")){
            $this->newclass     = VARS::get("NEWCLASS");
        }
        if (VARS::get("OLDCLASS")){
            $this->oldclass     = VARS::get("OLDCLASS");
        }
        $rtnRow = knja080bQuery::getNameMst();
        $this->schoolName           = $rtnRow["NAME1"];     // 学校区分
        $this->schoolDiv            = $rtnRow["NAMESPARE2"];// 1:併設校（法政・智辯） 2:中等教育学校（九段・自修館）

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput()
    {
        if (!isset($this->newclass)){
            $this->setWarning('MSG301');
            return false;
        }
        return true;
    }

    //新･旧クラス選択表示
    function &selectclass()
    {
        $db = Query::dbCheckOut();
        //新クラス読込み
        $this->getNewReadModel($db);
        //旧クラス読込み
        $this->getOldReadModel($db);

        Query::dbCheckIn($db);
    }
    //新クラス生徒読込
    function &getNewReadModel(&$db)
    {
        //新クラス一覧
        $result = $db->query(knja080bQuery::GetNewStudent($this));
        $i = 0;
        $this->schregno = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row = str_replace("'","\'",$row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAME"] = ($row["NAME_SHOW"]) ? $row["NAME_SHOW"] : $row["NAME"];
            $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
            $arg["NEW_OPTION"][$i] = array("TEXT"  => $row["HR_NAME"].$row["OLD_ATTENDNO"] ."番"."　".$remaingrade."　".$row["SCHREGNO"]."　".$row["NAME"],
                                           "VALUE" => $row["SCHREGNO"]
                                           );

            $i++;
            $this->schregno[] = $row["SCHREGNO"];
        }

        //インラインフレームに表示
        View::t_include("knja080bForm2.html", $arg);
    }
    //旧クラス生徒読込
    function &getOldReadModel(&$db)
    {
        //旧クラス一覧
        $result = $db->query(knja080bQuery::GetOldStudent($this));
        $i = 0;

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace("'","\'",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $remaingrade  = ($row["REMAINGRADE_FLG"] == "1") ? " [留] " : "　　　";
            $arg["OLD_OPTION"][$i] = array("TEXT"  => $row["HR_NAME"].$row["ATTENDNO"] ."番"."　".$remaingrade."　".$row["SCHREGNO"]."　". $row["NAME"],
                                           "VALUE" => $row["SCHREGNO"]
                                           );
            $i++;
        }
        //インラインフレームに表示
        View::t_include("knja080bForm2.html", $arg);
    }
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            list($div, $cd) = explode(":", $this->newclass);
            if ($div == "1") {
                knja080bQuery::getUpdateQueryGHR($this);    //指定年組が複式
            } else {
                knja080bQuery::getUpdateQuery($this);       //指定年組がＨＲ
            }
            $this->setMessage("MSG201");
        }
    }
}
?>
