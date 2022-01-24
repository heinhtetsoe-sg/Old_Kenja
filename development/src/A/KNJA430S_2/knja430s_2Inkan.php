<?php

require_once('for_php7.php');


class knja430s_2Inkan
{
    function main(&$model){

        $objForm = new form;
        $arg = array();

        //印鑑番号
        $arg["header"]["inkan_no"] = $model->req_inkan_no;
        //リターンURL
        $httpHost = VARS::server("HTTP_HOST");
        $ipAdress = VARS::server("SERVER_ADDR");
        $host = gethostbyaddr($ipAdress);
        $pass = REQUESTROOT ."/A/KNJA430S_2/knja430s_2index.php";
        $arg["header"]["returnurl"] = "http://" .$httpHost .$pass;
//        $arg["header"]["returnurl"] = "https://" .$httpHost .$pass;
        $arg["header"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJA430S_2"';
//echo '['.$arg["header"]["APPHOST"].']<hr>';

        //印影の表示
        if ($model->scan_result == "***") {
            $stampFile = $model->inkan_no .".bmp";
            $src = REQUESTROOT ."/image/stamp/" .$stampFile;
            $arg["data"]["IMAGE"] = '<image src="'.$src.'" alt="'.$stampFile.'" width="60" height="60">';
        }

        //印鑑番号
        $extra = "";
        $arg["data"]["INKAN_NO"] = knjCreateTextBox($objForm, $model->inkan_no, "inkan_no", 10, 8, $extra);

        $disRet = ($model->scan_result != "") ? "" : " disabled";
        $disGet = (($disRet == "")&&($model->scan_result != "non")&&($model->scan_result != "***")) ? "" : " disabled";

        //ファイル取込
        $extra = "onclick=\"return btn_submit('execute');\"".$disGet;
        $arg["button"]["BTN_FILE"] = knjCreateBtn($objForm, "BTN_FILE", "取 込", $extra);

        //戻る
        $extra = "onclick=\"return inkan_submit();\"".$disRet;
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "inkan_bmp", $model->inkan_bmp);
        knjCreateHidden($objForm, "scan_result", $model->scan_result);

        $arg["start"]   = $objForm->get_start("knja430s_2Inkan", "POST", "knja430s_2index.php", "", "knja430s_2Inkan");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja430s_2Inkan.html", $arg);
    }
}
?>