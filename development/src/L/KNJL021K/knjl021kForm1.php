<?php

require_once('for_php7.php');

class knjl021kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //試験区分
        $opt = array();
        $result = $db->query(knjl021kQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]    = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        $model->testdiv = ($model->testdiv == "") ? $opt[0]["value"] : $model->testdiv;
        $arg["TOP"]["TESTDIV"] = $this->createCombo($objForm, "TESTDIV", $model->testdiv, $opt, "Onchange=\"btn_submit('main');\"", 1);

        //中高判別フラグを作成する
        $row = $db->getOne(knjl021kQuery::GetJorH());
        $jhflg = ($row == 1) ? 1 : 2;

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "塾判定データをコピー";
        $arg["RIGHT_TITLE"] = "学校判定データをコピー";

        //対象データコンボ
        $opt_datacmb = $this->setWhereCmb($model, $jhflg);
        $arg["data"]["CENTER_TITLE"] = $this->createCombo($objForm, "CENTER_TITLE", $model->center_title, $opt_datacmb, "Onchange=\" return btn_submit('main');\"", 1);

        //コピー先指定ラジオ
        $opt_sitei    = array();
        $opt_sitei[0] = 1;
        $opt_sitei[1] = 2;
        $value = isset($model->field["OUTPUT_APT1"]) ? $model->field["OUTPUT_APT1"] : 1;
        $this->createRadio($objForm, $arg, "OUTPUT_APT", $value, "", $opt_sitei, get_count($opt_sitei));

        //リスト作成SQL用条件
        $opt_where = $this->makeWhereSql($model, $jhflg);
        //リスト作成
        $this->makeListData($objForm, $arg, $db, $model, $opt_where);

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成
        $this->makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["start"]    = $objForm->get_start("sel", "POST", "knjl021kindex.php", "", "sel");

        $arg["finish"]    = $objForm->get_finish();

        View::toHTML($model, "knjl021kForm1.html", $arg); 
    }

    //抽出条件コンボ作成
    function setWhereCmb(&$model, $jhflg)
    {
        $opt_datacmb = array();
        if ($jhflg == "1"){
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×", "value" => "0");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名○", "value" => "1");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名○", "value" => "2");
        }else {
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名○、出身学校○", "value" => "0");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名○、出身学校×", "value" => "1");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×、出身学校○", "value" => "2");
            $opt_datacmb[] = array("label" => "漢字氏名○、かな氏名×、出身学校×", "value" => "3");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名○、出身学校○", "value" => "4");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名○、出身学校×", "value" => "5");
            $opt_datacmb[] = array("label" => "漢字氏名×、かな氏名×、出身学校○", "value" => "6");
        }
        $model->center_title = (!$model->center_title) ? $opt_datacmb[0]["value"] : $model->center_title;

        return $opt_datacmb;
    }

    //リスト作成SQL用条件
    function makeWhereSql($model, $jhflg)
    {
        $opt_where = array();
        if ($jhflg == "1") {
            //漢字氏名○、かな氏名×
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'')");
            //漢字氏名×、かな氏名○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME_KANA,'') AS KANA1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME_KANA,'')",
                                 "SELECT_T_SELECT" => "T1.KANA1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.NAME2,'') != VALUE(L1.NAME,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");
            //漢字氏名○、かな氏名○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,''),VALUE(NAME_KANA,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1, T1.KANA1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");
        } else {
            //漢字氏名○、かな氏名○、出身学校○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1,VALUE(FS_CD,'') AS FS_CD1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,''),VALUE(NAME_KANA,''),VALUE(FS_CD,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1, T1.KANA1, T1.FS_CD1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");
            //漢字氏名○、かな氏名○、出身学校×
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,''),VALUE(NAME_KANA,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1, T1.KANA1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");
            //漢字氏名○、かな氏名×、出身学校○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1,VALUE(FS_CD,'') AS FS_CD1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,''),VALUE(FS_CD,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1, T1.FS_CD1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");
            //漢字氏名○、かな氏名×、出身学校×
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME,'') AS NAME1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME,'')",
                                 "SELECT_T_SELECT" => "T1.NAME1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'')");
            //漢字氏名×、かな氏名○、出身学校○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME_KANA,'') AS KANA1,VALUE(FS_CD,'') AS FS_CD1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME_KANA,''),VALUE(FS_CD,'')",
                                 "SELECT_T_SELECT" => "T1.KANA1, T1.FS_CD1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");
            //漢字氏名×、かな氏名○、出身学校×
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(NAME_KANA,'') AS KANA1",
                                 "MAIN_T_GROUP"    => "VALUE(NAME_KANA,'')",
                                 "SELECT_T_SELECT" => "T1.KANA1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");
            //漢字氏名×、かな氏名×、出身学校○
            $opt_where[] = array("MAIN_T_SELECT"   => "VALUE(FS_CD,'') AS FS_CD1",
                                 "MAIN_T_GROUP"    => "VALUE(FS_CD,'')",
                                 "SELECT_T_SELECT" => "T1.FS_CD1",
                                 "SELECT_T_WHERE"  => "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                                 "SELECT_JOIN"     => "VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");
        }
        return $opt_where;
    }

    //リスト作成
    function makeListData(&$objForm, &$arg, $db, $model, $opt_where)
    {
        $result = $db->query(knjl021kQuery::GetList($model, $opt_where));

        $opt_left   = array();
        $opt_center = array();
        $opt_right  = array();
        $valueno    = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["ACCEPTNO2"] == "ERROR"){
                $opt_center[] = array("label" => "●(".$row["ACCEPTNO1"]." ".$row["NAME1"]." ".$row["KANA1"].")-(重複データあり)",
                                                    "value" => sprintf('%03d',$valueno)."-".$row["ACCEPTNO1"]."-".$row["ACCEPTNO2"]);
            } else {
                $setBaseData = "";
                if ((in_array($row["ACCEPTNO1"], $model->err_selectdata)) ||
                    (in_array($row["ACCEPTNO2"], $model->err_selectdata))) {
                    //リストデータ設定(塾データ)
                    $setBaseData = (in_array($row["ACCEPTNO1"], $model->err_selectdata)) ? $row["ACCEPTNO1"]."/" : $row["ACCEPTNO2"]."/";
                    $this->setOpt($row, $valueno, $opt_left, $setBaseData);
                } else if ((in_array($row["ACCEPTNO1"], $model->err_selectdata3)) ||
                           (in_array($row["ACCEPTNO2"], $model->err_selectdata3))) {
                    //リストデータ設定(学校データ)
                    $setBaseData = (in_array($row["ACCEPTNO1"], $model->err_selectdata3)) ? $row["ACCEPTNO1"]."/" : $row["ACCEPTNO2"]."/";
                    $this->setOpt($row, $valueno, $opt_right, $setBaseData);
                } else {
                    //リストデータ設定
                    $this->setOpt($row, $valueno, $opt_center, $setBaseData);
                }
            }
            $valueno++;
        }
        $result->free();

        //オブジェクト作成
        $arg["main_part"] = array( "LEFT_PART"    => $this->createCombo($objForm, "LEFTLIST", "left", $opt_left, "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3(this, 'right','LEFTLIST','CENTERLIST',1);\"", 10),
                                   "CENTER_PART"  => $this->createCombo($objForm, "CENTERLIST", "left", $opt_center, "STYLE=\"WIDTH:100%\" WIDTH=\"100%\" onchange=\"listset();\"", 10),
                                   "RIGHT_PART"   => $this->createCombo($objForm, "RIGHTLIST", "right", $opt_right, "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3(this, 'left','CENTERLIST','RIGHTLIST',1);\"", 10),
                                   "SEL_ADD"      => $this->createBtn($objForm, "sel_add", "↑", "onclick=\"return move3(this, 'left','LEFTLIST','CENTERLIST',1);\""),
                                   "SEL_DEL"      => $this->createBtn($objForm, "sel_del", "↓", "onclick=\"return move3(this, 'right','LEFTLIST','CENTERLIST',1);\""),
                                   "SEL_ADD2"     => $this->createBtn($objForm, "sel_add2", "↑", "onclick=\"return move3(this, 'left','CENTERLIST','RIGHTLIST',1);\""),
                                   "SEL_DEL2"     => $this->createBtn($objForm, "sel_del2", "↓", "onclick=\"return move3(this, 'right','CENTERLIST','RIGHTLIST',1);\""),
                                    );
    }

    //リストデータ設定
    function setOpt($row, $valueno, &$opt, $setBaseData)
    {
        if (($row["FS_ACCEPTNO1"] != "" && $row["ACCEPTNO1"] != $row["FS_ACCEPTNO1"]) || 
            ($row["PS_ACCEPTNO1"] != "" && $row["ACCEPTNO1"] != $row["PS_ACCEPTNO1"]) ||
            ($row["FS_ACCEPTNO2"] != "" && $row["ACCEPTNO2"] != $row["FS_ACCEPTNO2"]) ||
            ($row["PS_ACCEPTNO2"] != "" && $row["ACCEPTNO2"] != $row["PS_ACCEPTNO2"]))
        {
            $opt[] = array("label" => $setBaseData."☆(".$row["ACCEPTNO1"]." ".$row["NAME1"]." ".$row["KANA1"].")-(".$row["ACCEPTNO2"]." ".$row["NAME2"]." ".$row["KANA2"].")",
                           "value" => sprintf('%03d',$valueno)."-".$row["ACCEPTNO1"]."-".$row["ACCEPTNO2"]);
        } else {
            $opt[] = array("label" => $setBaseData."　(".$row["ACCEPTNO1"]." ".$row["NAME1"]." ".$row["KANA1"].")-(".$row["ACCEPTNO2"]." ".$row["NAME2"]." ".$row["KANA2"].")",
                           "value" => sprintf('%03d',$valueno)."-".$row["ACCEPTNO1"]."-".$row["ACCEPTNO2"]);
        }
    }

    //ボタン作成
    function makeButton(&$objForm, &$arg, $model)
    {
        //保存ボタン
        $arg["button"]["BTN_OK"] = $this->createBtn($objForm, "btn_keep", "更 新", "onclick=\"return doSubmit();\"");
        //取消ボタン
        $arg["button"]["BTN_CLEAR"] = $this->createBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
        //終了ボタン
        $arg["button"]["BTN_END"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
        //チェックリスト
        $arg["button"]["BTN_CHECK"] = $this->createBtn($objForm, "BTN_CHECK", "チェックリスト", " onClick=\" wopen('../KNJL306K/knjl306kindex.php?,','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"");
    }

    //hidden作成
    function makeHidden(&$objForm)
    {

        $objForm->ae($this->createHiddenAe("cmd"));             //コマンド
        $objForm->ae($this->createHiddenAe("APT1", "　　　"));  //ラジオのラベル
        $objForm->ae($this->createHiddenAe("APT2", "　　　"));  //ラジオのラベル
        $objForm->ae($this->createHiddenAe("selectdata"));      //リスト用
        $objForm->ae($this->createHiddenAe("selectdata2"));     //リスト用
        $objForm->ae($this->createHiddenAe("selectdata3"));     //リスト用
    }

    //ラジオ作成
    function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
    {
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));
        for ($i = 1; $i <= $count; $i++) {
            $arg["data"][$name.$i] = $objForm->ge($name, $i);
        }
    }

    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}
?>
